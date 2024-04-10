package modules;

/**
 * The GlobalConstant class contains constant definitions used in the application.
 */
public final class GlobalConstant
{
    public final static String aggregation_server_ua = "AggregationServer";
    public final static String client_ua = "GETClient";
    public final static String content_server_ua = "ConetntServer";
    public final static String feed_store_file_name = "resources/feed_backup.json";
    public final static int feed_delete_delay = 30000; // 30s
    public final static int feed_size = 20;
    public final static int default_server_port = 4567;
    public final static String feed_store_file_encoding = "UTF-8";
    public final static String heartbeat_message = "live";
    public static enum StatusCodes
    {
        OK(200),
        CREATED(201),
        NO_CONTENT(204),
        BAD_REQUEST(400),
        INTERNAL_SERVER_ERROR(500);

        private final int value;

        private StatusCodes(int value)
        {
            this.value = value;
        }

        public int get_status_code()
        {
            return this.value;
        }

        public static StatusCodes value_of(int value)
        {
            for (StatusCodes code : StatusCodes.values())
            {
                if (code.get_status_code() == value)
                {
                    return code;
                }
            }
            return null;
        }
    }
}
