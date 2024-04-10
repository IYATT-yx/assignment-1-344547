package modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static modules.GlobalConstant.*;

/**
 * A class that represents a feed that aggregates requests from content servers.
 */
public class Feed
{
    HashMap<String, Request> feed_data_map;
    Timer timer;

    /**
     * Constructs a new feed and tries to restore data from backup files.
     */
    public Feed()
    {
        this.timer = new Timer();
        // try to read feed_data_map from file
        try
        {
            feed_data_map = JsonParser.json_file_to_feed_object();
            if (feed_data_map != null)
            {
                System.out.println("Create new aggregated feed from file.");
            }
            initTimer();
        }
        catch (Exception e)
        {
            System.out.println("Create a new aggregated feed");
            this.feed_data_map = new HashMap<>();
        }
    }

    /**
     * Initializes the timer with tasks to remove expired requests for each content server in the map.
     */
    private void initTimer()
    {
        for (String content_server : feed_data_map.keySet())
        {
            this.set_expire(content_server, feed_data_map.get(content_server).get_lamport_clock());
        }
    }

    /**
     * Adds a request to the feed data map and updates the timer task for the corresponding content server.
     * 
     * @param request The request to be added, which can be either a feed or a heartbeat.
     * @throws IOException Signals that an I/O exception of some sort has occurred. 
     *                  This class is the general class of exceptions produced by failed or interrupted I/O operations.
     */
    public void add_feed(Request request) throws IOException
    {

        // heart beat packet
        if (!request.get_feed().startsWith(heartbeat_message))
        {
            feed_data_map.put(request.get_UA(), request);
        }

        JsonParser.feed_object_to_json_file(feed_data_map);
        this.set_expire(request.get_UA(), request.get_lamport_clock());
    }

    /**
     * Sets a timer task to remove the request of a content server if it expires after a certain delay.
     * 
     * @param content_server_name The name of the content server whose request is to be removed.
     * @param lamport_clock The lamport clock of the request when it was added or updated.
     */
    public void set_expire(String content_server_name, int lamport_clock)
    {
        if (feed_data_map.containsKey(content_server_name))
        {
            feed_data_map.get(content_server_name).set_lamport_clock(lamport_clock);
        }
        // remove feed that is expred
        this.timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                synchronized (feed_data_map)
                {
                    // if already removed, return
                    if (!feed_data_map.containsKey(content_server_name))
                    {
                        return;
                    }
                    // if updated, return
                    if (!(feed_data_map.get(content_server_name).get_lamport_clock() == lamport_clock))
                    {
                        return;
                    }
                    // remvoe expired feed
                    feed_data_map.remove(content_server_name);
                }
                try
                {
                    JsonParser.feed_object_to_json_file(feed_data_map);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }, feed_delete_delay);
    }

    /**
     * Returns the text representation of the feed data map, sorted by the lamport
     * clock in descending order and limited to 20 entries.
     * 
     * @return A string that contains the feed data of the most recent 20 requests,
     *         separated by "entry" markers.
     */
    public String get_text()
    {
        // String feedText = "";
        ArrayList<Request> requests = new ArrayList<>();
        synchronized (this.feed_data_map)
        {
            for (String key : this.feed_data_map.keySet())
            {
                requests.add(feed_data_map.get(key));
            }
        }
        if (requests.size() == 0)
        {
            return "";
        }
        Collections.sort(requests);
        Collections.reverse(requests);

        while (requests.size() > feed_size)
        {
            requests.remove(requests.size() - 1);
        }

        // concat them
        String text = requests.get(0).get_feed();
        for (int i = 1; i < feed_size; ++i)
        {
            if (i >= requests.size())
            {
                break;
            }
            text += "\nentry\n";
            text += requests.get(i).get_feed();
        }

        return text;
    }
}
