package adeptius.swich;


import adeptius.exceptions.FunctionNotSupportedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class FoxGate extends Swich{

    public FoxGate(OutputStream outputStream, InputStream inputStream) {
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
        return "FoxGate";
    }

    @Override
    public void login() throws Exception {
        waitForString("me:");
        sendCommand(LOGIN+"\n");
        waitForString("Password:");
        sendCommand(PASS+"\n");
        waitForString(">");
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        String strPort = convertPort(port);
        sendCommand("show mac-address-table interface ethernet "+strPort+"\n");
        String s = waitForString(">");
        Matcher regexMatcher = Pattern.compile("Total entries: \\d{1,4}").matcher(s);
        while (regexMatcher.find()){
            String enties = regexMatcher.group().trim();
            enties = enties.replaceAll("   ", " ");
            enties = enties.replaceAll("  ", " ");
            return Integer.parseInt(enties.split(" ")[2]);
        }
        return 0;
    }
    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        String strPort = convertPort(port);
        sendCommand("show mac-address-table interface ethernet "+strPort+"\n");
        String s = waitForString(">");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}").matcher(s);
        while (regexMatcher.find()){
            macs.add(regexMatcher.group().replaceAll(":",""));
        }
        return macs;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("show interface br\n");
        String s = waitForStringWithContinue(">");
        ArrayList<Integer> downed = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("e0\\/\\d\\/\\d{1,2}.*(false|true)").matcher(s);
        while (regexMatcher.find()){
            String result = regexMatcher.group();
            result = result.replaceAll("      ", " ");
            result = result.replaceAll("     ", " ");
            result = result.replaceAll("    ", " ");
            result = result.replaceAll("   ", " ");
            result = result.replaceAll("  ", " ");
            String[] portsAndState = result.split(" ");

            if ("true".equals(portsAndState[2])){
                int port = convertPort(portsAndState[0]);
                downed.add(port);
            }
        }
        return downed;
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.substring(0,2) + ":"
                + mac.substring(2,4) + ":"
                + mac.substring(4,6) + ":"
                + mac.substring(6,8) + ":"
                + mac.substring(8,10) + ":"
                + mac.substring(10);
        sendCommand("show mac-address-table "+mac+"\n");
        String s = waitForStringWithContinue(">");
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}\\:){5}[0-9A-Fa-f]{2}[ ]*\\d{1,5}[ ]*0\\/\\d\\/\\d{1,2}").matcher(s);
        while (regexMatcher.find()){
            String result = regexMatcher.group();
            result = result.replaceAll("     ", " ");
            result = result.replaceAll("    ", " ");
            result = result.replaceAll("   ", " ");
            result = result.replaceAll("  ", " ");
            String[] portsAndState = result.split(" ");
            return convertPort(portsAndState[2]);
        }
        return null;
    }

    private String convertPort(int port){
        if (port <25){
            return "0/0/"+port;
        }else {
            return "0/1/"+ (port-24);
        }
    }

    private int convertPort(String port) {
        port = port.substring(2);
        String[] intAndPort = port.split("/");
        if (intAndPort[0].endsWith("0")) {
            return Integer.parseInt(intAndPort[1]);
        } else {
            return Integer.parseInt(intAndPort[1]) + 24;
        }
    }
}
