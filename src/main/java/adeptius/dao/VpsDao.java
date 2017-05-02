package adeptius.dao;



import org.apache.commons.codec.binary.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VpsDao {

    public static final String URL = "http://195.181.208.31/web/support/";

    public static String sendFeedBack(String nick, String text){
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("nick", nick);
            map.put("text", text);
            return sendPost(URL+"sendFeedback",map);
        }catch (Exception e){
            return "Что-то пошло не так..";
        }
    }

    public static String getValue(String key) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", key);
        return sendPost(URL+"getValue",map);
    }


    public static String sendPost(String url, HashMap<String, String> jSonQuery) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        String urlParameters = "";
        Object[] keys = jSonQuery.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            String key = (String) keys[i];
            urlParameters += key + "=" + jSonQuery.get(key);
            if (!(i == keys.length - 1)) urlParameters += "&";
        }
        con.setDoOutput(true);
        System.out.println("Передаю параметры: " + urlParameters);
        OutputStream os = con.getOutputStream();
        os.write(StringUtils.getBytesUtf8(urlParameters));
        os.flush();
        os.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();
//        result = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(result);
        byte[] bytes = result.getBytes();
        result = new String(bytes, "UTF-8");
        System.out.println("Ответ: " + result);
        in.close();
        return result;
    }


    static String getJsonFromUrl(String url, Map<String, String> keys) throws Exception {
        url = url + "?";
        for (Map.Entry<String, String> entry : keys.entrySet()) {
            String s = entry.getKey() + "=" + entry.getValue();
            url = url + s + "&";
        }

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();

//        result = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(result);
//        result = fixJson(result);
//        Utilits.networkLog("Получен Json: " + result);
        in.close();
        return result;
    }
}
