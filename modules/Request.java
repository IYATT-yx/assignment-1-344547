package modules;

import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.OutputStreamWriter;
import java.net.URL;

import modules.GlobalConstant.StatusCodes;

import static modules.GlobalConstant.*;;

/**
 * The Request class represents an HTTP request.
 * It provides methods for parsing and sending HTTP requests and responses,
 * as well as managing the underlying socket connection.
 */
public class Request implements Comparable<Request>, Cloneable, Serializable
{
    transient Socket socket;
    transient PrintWriter writer;
    transient BufferedReader reader;

    private String method;
    private int lamport_clock;
    private String user_agent;
    private String feed;
    private StatusCodes status_code;

    /**
     * Create a Request object based on the socket of the client connection.
     * @param scoket Socket for client connection
     */
    public Request(Socket socket)
    {
        this.socket = socket;
        try
        {
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Read the request sent by the client from the input stream of the socket and parse the request.
     */
    public void parse_request()
    {
        System.out.println("--------- Parse request ---------");
        try
        {
            for (String data = reader.readLine(); !data.trim().isEmpty(); data = reader.readLine()) // Read data from socket
            {
                System.out.println(data);

                // HTTP request method
                if (data.startsWith("GET"))
                {
                    this.method = "GET";
                }
                else if (data.startsWith("PUT"))
                {
                    this.method = "PUT";
                }
                else if (data.startsWith("Lamport-Clock")) // Lamport clock
                {
                    this.lamport_clock = Integer.parseInt(data.split(":")[1].trim());
                }
                else if (data.startsWith("User-Agent")) // UA
                {
                    this.user_agent = data.split(":")[1].trim();
                }
            }


            if (this.method.equals("PUT"))
            {
                String data = reader.readLine();
                if (data.trim().startsWith("<feed>"))
                {
                    this.feed = "";
                    while (!(data = reader.readLine()).startsWith("</feed>"))
                    {
                        this.feed += (data + "\n");
                    }
                }
                this.feed = this.feed.trim();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Read the response sent by the server from the input stream of the socket and parse the response.
     */
    public void parse_response()
    {
        if (this.user_agent.startsWith(client_ua))
        {
            System.out.println("-------------Client parse response-------------");
        }
        else
        {
            System.out.println("-------------ContentServer parse response-------------");
        }
        
        try
        {
            for (String data = reader.readLine(); !data.trim().isEmpty(); data = reader.readLine())
            {
                System.out.println(data);

                if (data.startsWith("HTTP"))
                {
                    this.status_code = StatusCodes.valueOf(data.split(" ")[1].trim());
                }
                else if (data.startsWith("Lamport-Clock"))
                {
                    this.lamport_clock = Integer.parseInt(data.split(":")[1].trim());
                }
                else if (data.startsWith("User-Agent"))
                {
                    this.user_agent = data.split(":")[1].trim();
                }
            }

            if (this.method.equals("GET")) // Read feed
            {
                String data = reader.readLine();
                if (data.trim().startsWith("<feed>"))
                {
                    this.feed = "";
                    while (!(data = reader.readLine()).startsWith("</feed>"))
                    {
                        this.feed += (data + "\n");
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.feed = this.feed.trim();
    }

    /**
     * Used to write data to the output stream of the socket.
     * @param data The data to be written.
     */
    void send(String data)
    {
        writer.write(data);
        writer.flush();
    }

    /**
     * Used to write server generated responses to the output stream of the socket.
     */
    public void send_response()
    {
        final String split = "\r\n";
        String data = "HTTP/1.1 " + this.status_code + split;
        data += ("User-Agent: " + aggregation_server_ua + split);
        data += ("Lamport-Clock: " + this.lamport_clock + split);

        if (this.feed != null)
        {
            data += split;
            data += ("<feed>" + split);
            data += (this.feed.trim() + split);
            data += ("</feed>" + split);
        }
        data += split;

        this.send(data);
        this.writer.close();
        try
        {
            if (!socket.isClosed())
            {
                socket.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Used to write client generated requests to the output stream of the socket.
     */
    public void send_request()
    {
        final String split = "\r\n";
        String data = this.method + " HTTP/1.1" + split;
        data += ("User-Agent: " + this.user_agent + split);

        if (this.feed != null)
        {
            data += ("Content-Type: text/plain" + split);
            data += ("Content-Length: " + this.feed.length() + 13 + 2*split.length() + split);
        } 

        data += ("Lamport-Clock: " + this.lamport_clock + split);

        if (this.feed != null)
        {
            data += (split + "<feed>" + split);
            data += (this.feed.trim() + split);
            data += ("</feed>" + split);
        }
        data += split;
        this.send(data);
    }

    /**
     * Returns a URL object based on the URL string.
     * @param url URL string.
     * @return success return URL object.
     *      failure return null;
     */
    public static URL get_url(String url)
    {
        if (!url.startsWith("http"))
        {
            url = "http://" + url;
        }

        try
        {
            URL res = new URL(url);

            // default port
            if (res.getPort() == -1)
            {
                return new URL(url + ":" + default_server_port);
            }
            return res;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;        
    }

    public void set(String method, int lamport_clock, String feed, String ua)
    {
        this.method = method;
        this.lamport_clock = lamport_clock;
        this.feed = feed;
        this.user_agent = ua;
    }

    public void set_status_code(StatusCodes status_code)
    {
        this.status_code = status_code;
    }

    public void set_lamport_clock(int lamport_clock)
    {
        this.lamport_clock = lamport_clock;
    }

    public StatusCodes get_status_code()
    {
        return this.status_code;
    }

    public String get_feed()
    {
        return this.feed;
    }

    public int get_lamport_clock()
    {
        return this.lamport_clock;
    }

    public String get_method()
    {
        return this.method;
    }

    public String get_UA()
    {
        return this.user_agent;
    }

    @Override
    public int compareTo(Request o)
    {
        Request r2 = o;
        return this.lamport_clock - r2.lamport_clock;
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}
