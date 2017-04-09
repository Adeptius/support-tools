package adeptius.dao;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class VpsDao {

    public static final String URL = "http://195.181.208.31/web/support/";


    public static String getValue(String key) throws Exception {
        HashMap<String, String> map = new HashMap<>();
        map.put("key", key);
        return sendPost(URL+"getValue",map);
    }

//    public static boolean isEnabled() throws Exception {
//        String response = getResponseFromUrl(URL+"isEnable");
//        return Boolean.parseBoolean(response);
//    }
//
//    public static int getLatestVersion() throws Exception {
//        String response = getResponseFromUrl(URL+"latestVersion");
//        return Integer.parseInt(response);
//    }


//    public static String getResponseFromUrl(String url) throws Exception {
//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        String result = "";
//        while (in.ready())
//            result += in.readLine();
//        byte[] bytes = result.getBytes();
//        result = new String(bytes, "UTF-8");
//        System.out.println("Получен Json: " + result);
//        in.close();
//        return result;
//    }

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
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        System.out.println("Передаю параметры: " + urlParameters);
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = in.readLine();
//        result = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(result);
        byte[] bytes = result.getBytes();
        result = new String(bytes, "UTF-8");
        System.out.println("Ответ: " + result);
        in.close();
        return result;
    }
}
