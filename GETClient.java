import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;

import modules.JsonParser;
import modules.LamportClock;
import modules.Request;
import modules.GlobalConstant.StatusCodes;

import static modules.GlobalConstant.client_ua;

/**
 * Implementation of GET client.
 */
public class GETClient
{
    LamportClock lamport_clock;
    String url;
    int port;
    int client_id;

    /**
     * Initialize client
     * @param url Server address.
     * @param port Server port.
     */
    GETClient(String url, int port)
    {
        this.url = url;
        this.port = port;
        Random random = new Random();
        this.client_id = random.nextInt(100000);
        this.lamport_clock = new LamportClock();
    }

    boolean get_feed(String file) throws UnknownHostException, IOException, IllegalAccessException
    {
        Socket socket = new Socket(this.url, this.port);
        Request request = new Request(socket);
        JsonParser jp = new JsonParser();

        request.set("GET", 0, null, client_ua + "/" + this.client_id);
        request.send_request();
        request.parse_response();

        String feed = request.get_feed();
        String[] jsons = JsonParser.get_json_from_GET(feed);
        
        for (String json : jsons)
        {
            jp.string_to_object(json);
            System.out.println(jp.object_to_format_string());
        }
        JsonParser.strings_to_json_file(jsons, file);

        if (request.get_status_code() == StatusCodes.OK)
        {
            return true;
        }
        else
        {
            System.out.println("Error: " + request.get_status_code());
            return false;
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: [Server address]:[Server port] [Weather File]");
            return;
        }

        try
        {
            URL url_obj = Request.get_url(args[0]);
            String url = url_obj.getHost();
            int port = url_obj.getPort();

            GETClient client = new GETClient(url, port);

            // Try to get feed for 5 times
            for (int i = 0; i < 5; ++i)
            {
                if (client.get_feed(args[1]))
                {
                    break;
                }
                System.out.print("....retry....");
                Thread.sleep(3000);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
