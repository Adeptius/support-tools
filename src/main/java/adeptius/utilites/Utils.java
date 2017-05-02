package adeptius.utilites;

import adeptius.javafx.Gui;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;


public class Utils {

    public static HashMap<String, String[]> getServersAndIp() {
        HashMap<String, String[]> map = new HashMap<>();
        map.put("Троещина, Бобровица", new String[]{"10.110.0.12"});
        map.put("Львов", new String[]{"10.110.0.7"});
        map.put("Мелитополь", new String[]{"10.110.0.8"});
        map.put("Руденко, Акимовка, Кринички", new String[]{"10.110.0.3"});
        map.put("Житомир, Черняхов", new String[]{"10.110.0.10"});
        map.put("Днепр", new String[]{"10.110.0.9"});
        map.put("Драгоманова", new String[]{"10.110.0.2"});
        map.put("Кривой рог", new String[]{"10.110.0.11"});
        map.put("Бердычев", new String[]{"10.110.0.6"});
        map.put("Ивано-франковск", new String[]{"10.110.0.13"});
        map.put("Киев, Акимовка, Кринички, Бобровица", new String[]{"10.110.0.3", "10.110.0.2", "10.110.0.12"});
        return map;
    }

    public static String copyIPerf() throws Exception{
        String tempDir = System.getProperty("java.io.tmpdir");
        String filename = "iperf.exe";
        Files.copy(Gui.class.getResourceAsStream(filename), Paths.get(tempDir + "iperf.exe"), StandardCopyOption.REPLACE_EXISTING);
        return tempDir + filename;
    }
}
