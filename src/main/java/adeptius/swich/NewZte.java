package adeptius.swich;


import adeptius.exceptions.FunctionNotSupportedException;
import adeptius.exceptions.SimultaneousConfigException;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("Duplicates")
public class NewZte extends Swich {

    public NewZte(OutputStream outputStream, InputStream inputStream) {
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
        if (port > 24) {
            throw new OperationNotSupportedException();
        }
        sendCommand("set dhcp snooping del port " + port + "\n");
        waitForString("#");
        sendCommand("set dhcp ip-source-guard del port " + port + "\n");
        waitForString("#");
        sendCommand("set dhcp option82 del port " + port + " \n");
        waitForString("#");
    }

    @Override
    public void makeDhcpOnPort(int port) throws Exception {
        if (port > 24) {
            throw new OperationNotSupportedException();
        }
        sendCommand("set dhcp snooping-and-option82 enable\n");
        waitForString("#");
        sendCommand("set port " + port + " acl " + port + " dis\n");
        waitForString("#");
        sendCommand("set dhcp port " + port + " client \n");
        waitForString("#");
        sendCommand("set dhcp snooping add port " + port + " \n");
        waitForString("#");
        sendCommand("set dhcp ip-source-guard add port " + port + "\n");
        waitForString("#");
        sendCommand("set dhcp option82 add port " + port + "\n");
        waitForString("#");
        sendCommand("set dhcp option82 sub-option port " + port + " circuit-ID on cisco\n");
        waitForString("#");
    }

    @Override
    public String toString() {
        return "NewZte";
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        sendCommand("sh mac dy po "+port+"\n");
        String s = waitForString("#");
        Matcher regexMatcher = Pattern.compile("Total MAC Address : \\d{1,4}").matcher(s);
        while (regexMatcher.find()){
            String str = regexMatcher.group().trim();
            str = str.replaceAll("   ", " ");
            str = str.replaceAll("  ", " ");
            return Integer.parseInt(str.split(" ")[4]);
        }
        return 0;
    }

    @Override
    public void login() throws Exception {
        waitForString("login:");
        sendCommand(LOGIN+"\n");
        waitForString("password:");
        sendCommand(PASS+"\n");
        waitForString(">","#");
        sendCommand("ena\n");
        String s = waitForString("password:","#", "Simultaneous");
        if (s.contains("Simultaneous")){
            throw new SimultaneousConfigException();
        }
        sendCommand("rbnfqcrbqbynthyt\n");
        waitForString("#");
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        sendCommand("sh mac dy po " + port + "\n");
        String s = waitForString(">", "#");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("[0-9A-Fa-f]{4}\\.[0-9A-Fa-f]{4}\\.[0-9A-Fa-f]{4}").matcher(s);
        while (regexMatcher.find()){
            macs.add(regexMatcher.group().replaceAll("\\.",""));
        }
        return macs;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("sh port \n");
        String s = waitForStringWithContinue(">", "#");
        int position = 0;
        ArrayList<Integer> downed = new ArrayList<>();
        int currentPort = 0;
        while (s.contains("PortEnable")){
            position = s.indexOf("PortId");
            s=s.substring(position+3);
            s=s.substring(s.indexOf(":"));
            String stringWithPort = s.substring(1,5).trim();
            currentPort = Integer.parseInt(stringWithPort);
            s=s.substring(s.indexOf("PortEnable"));
            s=s.substring(s.indexOf(":"));
            String stringWithStatus = s.substring(1,9).trim();
            if (stringWithStatus.equals("disable")){
                downed.add(currentPort);
            }
        }
        return downed;
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.replaceAll(":","").replaceAll("-","").replaceAll("\\.","");
        String fixedMac = mac.substring(0,2) + "."
                + mac.substring(2,4) + "."
                + mac.substring(4,6) + "."
                + mac.substring(6,8) + "."
                + mac.substring(8,10) + "."
                + mac.substring(10);
        sendCommand("sh mac mac " + fixedMac + "\n" );
        String s = waitForString(">", "#");
        Matcher regexMatcher = Pattern.compile("port-\\d{1,2}").matcher(s);
        if (regexMatcher.find()){
            return new Integer(regexMatcher.group().substring(5));
        }
        return null;
    }
}
