package modules;

import java.util.concurrent.PriorityBlockingQueue;

import static modules.GlobalConstant.*;

/**
 * A class that extends the Thread class and handles requests from a priority
 * blocking queue.
 */
public class HandlerThread extends Thread
{
    PriorityBlockingQueue<Request> queue;
    LamportClock lamport_clock;
    Feed feed;

    /**
     * Constructs a new handler thread with the given queue and lamport clock.
     * @param queue The priority blocking queue that stores the requests to be handled.
     * @param lamport_clock The lamport clock of the handler thread.
     */
    public HandlerThread(PriorityBlockingQueue<Request> queue, LamportClock lamport_clock)
    {
        this.queue = queue;
        this.lamport_clock = lamport_clock;
        this.feed = new Feed();
    }

    @Override
    public void run()
    {
        System.out.println("-------------handler start-----------");
        while (true)
        {
            Request request = null;
            try
            {
                request = queue.take();
                System.out.println("\n\n------------handle new request-------------");
                // get request
                if (request.get_method().equals("GET"))
                {
                    handle_GET(request);
                }
                // put request
                else if (request.get_method().equals("PUT"))
                {
                    handle_PUT(request);
                }
                else
                {
                    handle_error(request, StatusCodes.BAD_REQUEST);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                try
                {
                    handle_error(request, StatusCodes.INTERNAL_SERVER_ERROR);
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * Handles a GET request by sending back the feed data as a response.
     * @param request The GET request to be handled.
     */
    public void handle_GET(Request request)
    {
        request.set(null, this.lamport_clock.get_and_add_clock(), this.feed.get_text(), aggregation_server_ua);
        request.set_status_code(StatusCodes.OK);
        request.send_response();
    }

    /**
     * Handles a PUT request by adding it to the feed data and sending back an
     * acknowledgment response.
     * @param request The PUT request to be handled, which can be either a feed or a heartbeat.
     */
    public void handle_PUT(Request request)
    {
        try
        {
            // no content
            if (request.get_feed() == null || request.get_feed().trim().length() == 0)
            {
                this.handle_error(request, StatusCodes.NO_CONTENT);
                return;
            }

            // heartbeat
            feed.add_feed((Request) request.clone());

            request.set(null, this.lamport_clock.get_and_add_clock(), null, aggregation_server_ua);
            if (request.get_lamport_clock() == 0)
            {
                request.set_status_code(StatusCodes.CREATED);
            }
            else
            {
                request.set_status_code(StatusCodes.OK);
            }

            request.send_response();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Handles an error by sending back an error response with the given code.
     * @param request The request that caused the error.
     * @param error_code The error code to be sent back as a response status code.
     */
    public void handle_error(Request request, StatusCodes error_code)
    {
        request.set_status_code(error_code);
        request.set(null, this.lamport_clock.get_and_add_clock(), null, aggregation_server_ua);
        request.send_response();
    }
}