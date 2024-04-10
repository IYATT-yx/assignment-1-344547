import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;

import modules.LamportClock;
import modules.Request;
import modules.HandlerThread;
import static modules.GlobalConstant.*;

/**
 * A class that extends the Thread class and represents an aggregation server
 * that receives requests from content servers and clients and sends back
 * responses.
 */
public class AggregationServer extends Thread
{
    public static void main(String[] args) throws IOException
    {
        int port = default_server_port;
        if (args.length == 1)
        {
            port = Integer.parseInt(args[0]);
        }
        
        System.out.println("------- Server start ----------");

        PriorityBlockingQueue<Request> queue = new PriorityBlockingQueue<>();
        LamportClock lamport_clock = new LamportClock();

        // start a thread to create response
        HandlerThread t = new HandlerThread(queue, lamport_clock);
        t.setDaemon(true);
        t.start();

        try(ServerSocket serverSocket = new ServerSocket(port))
        {
            Socket client_socket = null;

            while ((client_socket = serverSocket.accept()) != null)
            {
                try
                {
                    System.out.println("----------Get a new client----------------");
                    Request request = new Request(client_socket);
                    request.parse_request();

                    // update lamport lamport_clock for receive event
                    lamport_clock.compare_and_set(request.get_lamport_clock());

                    // put to ordered request queue
                    queue.add(request);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }
}
