package adeptius.swich;


import adeptius.exceptions.FunctionNotSupportedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Linksys extends Swich {

    public Linksys(OutputStream outputStream, InputStream inputStream) {
        super(outputStream, inputStream);
    }

    @Override
    public void makeStaticOnPort(int port) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public void makeDhcpOnPort(int port) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public String toString() {
        return "Linksys";
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        return getMacAddress(port).size();
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.replaceAll(":","").replaceAll("-","").replaceAll("\\.","");
        String fixedMac = mac.substring(0,2) + ":"
                + mac.substring(2,4) + ":"
                + mac.substring(4,6) + ":"
                + mac.substring(6,8) + ":"
                + mac.substring(8,10) + ":"
                + mac.substring(10);
        sendCommand("sh bri add add " + fixedMac + "\n");
        String s = waitForString("console#");
        String[] splitted = s.split("\n");
        for (String s1 : splitted) {
            if (s1.contains(fixedMac + "  ")){
                s1 =s1.replaceAll("       ", " ");
                s1 =s1.replaceAll("      ", " ");
                s1 =s1.replaceAll("     ", " ");
                s1 =s1.replaceAll("    ", " ");
                s1 =s1.replaceAll("   ", " ");
                s1 =s1.replaceAll("  ", " ");
                String[] spl = s1.split(" ");
                Integer down;
                String port = spl[3];
                if (port.startsWith("e")){
                    down = Integer.parseInt(port.substring(1));
                }else {
                    down = Integer.parseInt(port.substring(1)) + 24;
                }
                return down;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("sh int con\n");
        String s = waitForStringWithContinue("console#");
        s =s.replaceAll("       ", " ");
        s =s.replaceAll("      ", " ");
        s =s.replaceAll("     ", " ");
        s =s.replaceAll("    ", " ");
        s =s.replaceAll("   ", " ");
        s =s.replaceAll("  ", " ");
        String[] splitted = s.split("\n");
        ArrayList<Integer> downed = new ArrayList<>();
        for (String s1 : splitted) {
            if (s1.startsWith("e") || s1.startsWith("g")){
                String[] spl = s1.split(" ");
                String status = spl[6];
                if ("Down".equals(status)){
                    int down;
                    String port = spl[0];
                    if (port.startsWith("e")){
                        down = Integer.parseInt(port.substring(1));
                    }else {
                        down = Integer.parseInt(port.substring(1)) + 24;
                    }
                    downed.add(down);
                }
            }
        }
        return downed;
    }

    @Override
    public void login() throws Exception{
        waitForString("Name:");
        sendCommand(LOGIN+"\n");
        waitForString("Password:");
        sendCommand(PASS+"\n");
        waitForString("console#");
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        String eth = port>24? "g"+(port-24) : "e"+port;
        sendCommand("sh bri add e "+eth+"\n");
        String s = waitForString("console#");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}\\:){5}[0-9A-Fa-f]{2}").matcher(s);
        while (regexMatcher.find()){
            macs.add(regexMatcher.group().replaceAll(":",""));
        }
        return macs;
    }
}
