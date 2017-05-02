package adeptius.swich;


import adeptius.exceptions.FunctionNotSupportedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class Dlink extends Swich {


    public Dlink(OutputStream outputStream, InputStream inputStream) {
        super(outputStream, inputStream);
    }

    @Override
    public String findMacBdCom(String mac) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public void portUp(int port) throws Exception {
        throw new FunctionNotSupportedException();
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
    public void login() throws Exception {
        waitForString("UserName:", "username");
        sendCommand(LOGIN+"\n");
        waitForString("PassWord:", "password");
        sendCommand(PASS+"\n");
        waitForString("#");
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        sendCommand("sh fdb port "+port+"\n");
        String s = waitForString("#");
        Matcher regexMatcher = Pattern.compile("Total Entries : \\d{1,4}").matcher(s);
        while (regexMatcher.find()){
            String str = regexMatcher.group().trim();
            str = str.replaceAll("   ", " ");
            str = str.replaceAll("  ", " ");
            return Integer.parseInt(str.split(" ")[3]);
        }
        return 0;
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        sendCommand("sh fdb port "+port+"\n");
        String s = waitForString("#");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}").matcher(s);
        while (regexMatcher.find()){
            macs.add(regexMatcher.group().replaceAll("-",""));
        }
        if (macs.size()==0){
            sendCommand("sh add bl all\n");
            s = waitForString("#");
            regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}.*\\d{1,2}").matcher(s);
            while (regexMatcher.find()){
                String result = regexMatcher.group().replaceAll("-","");
                int foundedPort = Integer.parseInt(result.substring(result.lastIndexOf(" ")).trim());
                if (foundedPort==port){
                    result = result.substring(0, result.lastIndexOf(" ")).trim();
                    macs.add(result);
                }
            }
        }
        return macs;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("sh ports 1-9\n");
        String s = waitForString("SPACE");
        sendCommand("sh ports 10-14\n");
        s = s + waitForString("SPACE");
        sendCommand("sh ports 15-18\n");
        s = s + waitForString("SPACE");
        sendCommand("sh ports 19-24\n");
        s = s + waitForString("SPACE", "Next possible");
        ArrayList<Integer> downed = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("\\d{1,2}[ ]*(Enabled|Disabled)").matcher(s);
        while (regexMatcher.find()){
            String result = regexMatcher.group();
            result = result.replaceAll("      ", " ");
            result = result.replaceAll("     ", " ");
            result = result.replaceAll("    ", " ");
            result = result.replaceAll("   ", " ");
            result = result.replaceAll("  ", " ");
            String[] portsAndState = result.split(" ");
            if (portsAndState[1].equals("Disabled")){
                downed.add(Integer.parseInt(portsAndState[0]));
            }
        }
        return downed;
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.replaceAll(":","").replaceAll("-","").replaceAll("\\.","").toUpperCase();
        mac = mac.substring(0,2) + "-"
                + mac.substring(2,4) + "-"
                + mac.substring(4,6) + "-"
                + mac.substring(6,8) + "-"
                + mac.substring(8,10) + "-"
                + mac.substring(10);
        sendCommand("sh fdb mac " + mac + "\n");
        String s = waitForString("#");
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}[ ]*\\d{1,2}").matcher(s);
        while (regexMatcher.find()){
            s = regexMatcher.group().replaceAll("-","");
            s =s.replaceAll("    ", " ");
            s =s.replaceAll("   ", " ");
            s =s.replaceAll("  ", " ");
            return new Integer(s.trim().toLowerCase().split(" ")[1]);
        }

        sendCommand("sh add bl all\n");
        s = waitForString("#");
        regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}.*\\d{1,2}").matcher(s);
        while (regexMatcher.find()){
            String result = regexMatcher.group().trim();
            if (result.contains(mac)){
                return new Integer(result.split(" ")[1].trim());
            }
        }

        return null;
    }
}
