package adeptius.swich;

import adeptius.exceptions.FunctionNotSupportedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaiseCom extends Swich {

    public RaiseCom(OutputStream outputStream, InputStream inputStream) {
        super(outputStream, inputStream);
    }

    @Override
    public void makeDhcpOnPort(int port) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public void makeStaticOnPort(int port) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public void login() throws Exception {
        waitForString("Login:");
        sendCommand(LOGIN + "\n");
        waitForString("Password:");
        sendCommand(PASS + "\n");
        String s = waitForString(">", "#","Login:");
        if (s.endsWith("Login:")){
            sendCommand("duty\n");
            waitForString("Password:");
            sendCommand("support\n");
            s = waitForString(">", "#");
        }
        if (s.endsWith(">")) {  // если после залогинивания мы не в энейбле
            sendCommand("enable\n");
            waitForString("Password:");
            sendCommand("zxr10\n");
            s = waitForString("#", ">");
            if (s.endsWith(">")) { // если после ввода zxr10 мы не попали в энейбл
                sendCommand("enable\n");
                waitForString("Password:");
                sendCommand("raisecom\n");
                waitForString("#");
            }
        }
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        sendCommand("show mac-address-table l2-address port " + port + "\n");
        String s = waitForString("#");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{4}.){2}[0-9A-Fa-f]{4}").matcher(s);
        while (regexMatcher.find()) {
            macs.add(regexMatcher.group().replaceAll("\\.", "").toLowerCase());
        }
        return macs;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("show interface port\n");
        String s = waitForStringWithContinue("#");
        ArrayList<Integer> downed = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("\\d{1,2}[ ]*(enable|disable)").matcher(s);
        while (regexMatcher.find()) {
            String result = regexMatcher.group();
            result = result.replaceAll("     ", " ");
            result = result.replaceAll("    ", " ");
            result = result.replaceAll("   ", " ");
            result = result.replaceAll("  ", " ");
            String[] portsAndState = result.split(" ");
            if (portsAndState[1].equals("disable")) {
                downed.add(Integer.parseInt(portsAndState[0]));
            }
        }
        return downed;
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.substring(0, 4) + "."
                + mac.substring(4, 8) + "."
                + mac.substring(8);
        sendCommand("sh mac-address-table l2-address | include " + mac.toUpperCase() + "\n");
        String s = waitForString("#");
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{4}.){2}[0-9A-Fa-f]{4}[ ]*\\d{1,2}").matcher(s);
        while (regexMatcher.find()) {
            String result = regexMatcher.group();
            result = result.replaceAll("     ", " ");
            result = result.replaceAll("    ", " ");
            result = result.replaceAll("   ", " ");
            result = result.replaceAll("  ", " ");
            String[] portsAndState = result.split(" ");
            return Integer.parseInt(portsAndState[1]);
        }
        return null;
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        return getMacAddress(port).size();
    }
}
