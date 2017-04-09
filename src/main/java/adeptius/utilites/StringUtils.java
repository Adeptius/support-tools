package adeptius.utilites;


import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String cleanMac(String mac){
        return mac.replaceAll(":","")
                .replaceAll("-","")
                .replaceAll("\\.","")
                .toLowerCase()
                .trim();
    }

    public static ArrayList<String> getSwitchesFromString(String sw){
        ArrayList<String> switches = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}").matcher(sw);
        while (regexMatcher.find()){
            switches.add(regexMatcher.group());
        }
        return switches;
    }

    public static String convertArrToString(String[] arr){
        String result = "";
        for (String s : arr) {
            result += s + "\n";
        }
        return result;
    }
}
