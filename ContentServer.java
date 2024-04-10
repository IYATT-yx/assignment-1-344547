import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import modules.JsonParser;
import modules.LamportClock;
import modules.Request;
import static modules.GlobalConstant.*;

/**
 * Represents a content server that can send messages to an aggregation server.
 */
public class ContentServer
{
    String url;
    int port;
    LamportClock lamport_clock;
    String feed_name;
    String client_id;

    /**
     * Constructs a new content server with the given URL and port number.
     * @param url The URL of the feed server.
     * @param port The port number of the feed server.
     */
    ContentServer(String url, int port, String client_id)
    {
        this.url = url;
        this.port = port;
        this.client_id = client_id;
        this.lamport_clock = new LamportClock();
    }

    /**
     * Sends a message to the feed server and returns true if successful, false otherwise.
     * @param msg The message to be sent, which can be either a feed or a heartbeat.
     * @return A boolean value indicating whether the message was sent successfully or not.
     */
    boolean send_message(String msg)
    {
        try
        {
            Socket socket = new Socket(this.url, this.port);
            Request request = new Request(socket);

            request.set("PUT", this.lamport_clock.get_and_add_clock(), msg, content_server_ua + "/" + this.client_id);

            String info = "Sending heartbeat";
            if (!msg.startsWith(heartbeat_message))
            {
                info = "Sending feed";
            }

            System.out.println(info);
            request.send_request();
            request.parse_response();

            this.lamport_clock.compare_and_set(request.get_lamport_clock());

            System.out.println("http code: " + request.get_status_code());
            System.out.println("time stamp: " + request.get_lamport_clock());
            System.out.println();
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * The main method that runs the content server and sends messages to the feed server periodically.
     * @param args The command-line arguments, which should contain two strings: 
     *             the URL and port number of the feed server, and the file name of the weather data.
     */
    public static void main(String[] args)
    {
        if (args.length != 3)
        {
            System.out.println("Usage: [Server address]:[Server port] [Client ID] [Weather data file]");
            return;
        }

        try
        {
            URL url_obj = Request.get_url(args[0]);
            String url = url_obj.getHost();
            int port = url_obj.getPort();
            JsonParser jp = new JsonParser();

            ContentServer content_server = new ContentServer(url, port, args[1]);

            while (true)
            {
                jp.key_value_pair_text_file_to_object(args[2]);
                String feed = jp.object_to_string();
                while (true)
                {
                    if (content_server.send_message(feed))
                    {
                        break;
                    }
                    Thread.sleep(3000);
                }

                while (true)
                {
                    content_server.send_message(heartbeat_message);
                    Thread.sleep(3000);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}