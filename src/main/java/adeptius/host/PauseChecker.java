package adeptius.host;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class PauseChecker {

    public static PauseState getState(String php) throws Exception {
        URL obj = new URL("http://host.o3.ua/support/js_phonestate.php");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Cookie", "PHPSESSID=" + php);
        String urlParameters = "";
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String result = "";
        while (in.ready()) {
            result += in.readLine();
        }

        in.close();

        if (result.startsWith("<div style=\"margin: 16px auto 8px")){
            return PauseState.SESSION_ERROR;
        }

        if (result.contains("play button")){
            return PauseState.ON_PAUSE;
        }

        if (result.contains("pause button")){
            return PauseState.CAN_STOP;
        }
        System.out.println(result);
        return PauseState.WORKING;
    }
}
