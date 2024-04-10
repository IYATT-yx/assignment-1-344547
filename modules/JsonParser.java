package modules;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import static modules.GlobalConstant.*;

/**
 * JSON representation format for weather information.
 */
class JsonFormat
{
    private String id;
    private String name;
    private String state;
    private String time_zone;
    private float lat;
    private float lon;
    private String local_date_time;
    private String local_date_time_full;
    private float air_temp;
    private float apparent_t;
    private String cloud;
    private float dewpt;
    private float press;
    private float rel_hum;
    private String wind_dir;
    private float wind_spd_kmh;
    private int wind_spd_kt;
}

/**
 * The JsonParser class is responsible for parsing JSON data and converting it to corresponding Java objects.
 * It provides methods to convert key-value pair text files to JSON objects, JSON objects to JSON strings,
 * JSON strings to JSON objects, and formatting JSON objects into strings. It also includes methods for writing
 * JSON objects to JSON files and reading JSON files into JSON objects.
 */
public class JsonParser
{
    private JsonFormat json_format = null;
    private final static Gson gson = new Gson();

    /**
     * Initialize Json parser
     */
    public JsonParser()
    {
        json_format = new JsonFormat();
    }

    /**
     * Key value pair text file to Json object.
     * @param file_name
     */
    public void key_value_pair_text_file_to_object(String file_name)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(file_name)))
        {
            for (String line = br.readLine(); line != null; line = br.readLine())
            {
                if (line.isEmpty())
                {
                    continue;
                }

                String[] parts = line.split(":", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.isEmpty() || value.isEmpty())
                {
                    continue;
                }

                Field field = this.json_format.getClass().getDeclaredField(key);
                Class<?> type = field.getType();
                field.setAccessible(true);

                if (type == int.class)
                {
                    field.setInt(this.json_format, Integer.parseInt(value));
                }
                else if (type == float.class)
                {
                    field.setFloat(this.json_format, Float.parseFloat(value));
                }
                else if (type == String.class)
                {
                    field.set(this.json_format, value);
                }
                else
                {
                    throw new IllegalArgumentException("Unsupported type: " + type);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Json object to Json string.
     * @return Json string.
     */
    public String object_to_string()
    {
        return gson.toJson(this.json_format);
    }

    /**
     * Json string to Json object.
     * @param json_string
     *            Json string.
     */
    public void string_to_object(String json_string)
    {
        this.json_format = gson.fromJson(json_string, json_format.getClass());
    }

    /**
     * Format the JSON object into a string.
     * @return success return a string.
     *         failure return null.
     */
    public String object_to_format_string()
    {
        try
        {
            Class<?> cls = json_format.getClass();
            Field[] fields = cls.getDeclaredFields();
            StringBuilder sb = new StringBuilder();
            sb.append("------------------------------------------------------------------------\n");
            for (Field filed : fields)
            {
                filed.setAccessible(true);
                String name = filed.getName();
                Object value = filed.get(json_format);
                sb.append(String.format("| %-20s | %-45s |\n", name, value));
                sb.append("------------------------------------------------------------------------\n");
            }
            String result = sb.toString();
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Json object to Json file
     * @param json_file_name
     */
    public static synchronized void strings_to_json_file(String[] jsons, String json_file_name)
    {
        Gson gson_builder = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(json_file_name),
                feed_store_file_encoding))
        {
            gson_builder.toJson(jsons, writer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Json file to Json object
     * @param json_file_name
     */
    public static synchronized String[] json_file_to_strings(String json_file_name)
    {
        try
        {
            String json = new String(Files.readAllBytes(Paths.get(json_file_name)));
            return gson.fromJson(json, String[].class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Match the Json field from the GET content.
     * @param feed
     * @return A string array of Json fields.
     */
    public static String[] get_json_from_GET(String feed)
    {
        Pattern pattern = Pattern.compile("\\{.*?\\}");
        Matcher matcher = pattern.matcher(feed);
        ArrayList<String> jsons = new ArrayList<>();
        while (matcher.find())
        {
            jsons.add(matcher.group());
        }
        return jsons.toArray(new String[jsons.size()]);
    }

    /**
     * Feed data map to Json file.
     * @param map
     */
    public static synchronized void feed_object_to_json_file(HashMap<String, Request> map)
    {
        Gson gson_builder = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(feed_store_file_name),
                feed_store_file_encoding))
        {
            gson_builder.toJson(map, writer);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Json file to Feed data map
     * 
     * @return success return feed data map.
     *         failure return null.
     */
    public static synchronized HashMap<String, Request> json_file_to_feed_object()
    {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(feed_store_file_name),
                feed_store_file_encoding))
        {
            return gson.fromJson(reader, new TypeToken<HashMap<String, Request>>()
            {
            }.getType());
        }
        catch (FileNotFoundException e)
        {
            System.out.println("feed backup file does not exist");
            return null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
