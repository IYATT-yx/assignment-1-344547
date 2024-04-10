import java.util.Arrays;

import modules.JsonParser;

public class TestCompare
{
    public static void main(String[] args)
    {
        String[] weather_files = 
        {
            "resources/weather1.txt",
            "resources/weather2.txt",
            "resources/weather3.txt",
            "resources/weather4.txt",
            "resources/weather5.txt"
        };

        JsonParser jp = new JsonParser();
        String[] weather_strings = new String[5];
        int counter = 0;
        for (String weather_file : weather_files)
        {
            jp.key_value_pair_text_file_to_object(weather_file);
            weather_strings[counter++] = jp.object_to_string();
        }
        Arrays.sort(weather_strings);

        String[] client_strings = JsonParser.json_file_to_strings("resources/client_out.json");
        Arrays.sort(client_strings);
        if (client_strings.length == 0)
        {
            System.out.println("------------------------------------------------------------");
            System.out.println("The client cannot obtain data.\n" +
                                "If this is the result after the content server is disconnected for more than 30 seconds,\n" +
                                "it proves that the aggregation server has cleared expired data normally.");
            System.out.println("------------------------------------------------------------");
            return;
        }

        if (Arrays.equals(weather_strings, client_strings))
        {
            System.out.println("********************success********************");
        }
        else
        {
            System.out.println("********************failure********************");
        }
    }
}