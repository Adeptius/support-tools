package adeptius.swich;

import adeptius.exceptions.FunctionNotSupportedException;

import javax.naming.OperationNotSupportedException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings("Duplicates")
public class OldZte extends Swich {

    public OldZte(OutputStream outputStream, InputStream inputStream) {
        super(outputStream, inputStream);
    }

    @Override
    public String findMacBdCom(String mac) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public void portUp(int port) throws Exception {
        throw new FunctionNotSupportedException();
//        sendCommand("set port 22 enable\n");
//        waitForString("#");
//        Thread.sleep(2000);
//        sendCommand("set port 22 pvid 595\n\n");
//        waitForString("#");
    }

    @Override
    public String toString() {
        return "OldZte";
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
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        sendCommand("sh fdb port " + port + "\n");
        String s = waitForString(">","#");
        Matcher regexMatcher = Pattern.compile("Total: \\d{1,4}").matcher(s);
        while (regexMatcher.find()) {
            String str = regexMatcher.group().trim();
            str = str.replaceAll("   ", " ");
            str = str.replaceAll("  ", " ");
            return Integer.parseInt(str.split(" ")[1]);
        }
        return 0;
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        mac = mac.replaceAll(":", "").replaceAll("-", "").replaceAll("\\.", "");
        String fixedMac = mac.substring(0, 2) + "."
                + mac.substring(2, 4) + "."
                + mac.substring(4, 6) + "."
                + mac.substring(6, 8) + "."
                + mac.substring(8, 10) + "."
                + mac.substring(10);
        sendCommand("sh fdb mac " + fixedMac + "\n");
        String s = waitForString(">","#");
        s = s.replaceAll("       ", " ");
        s = s.replaceAll("      ", " ");
        s = s.replaceAll("     ", " ");
        s = s.replaceAll("    ", " ");
        s = s.replaceAll("   ", " ");
        s = s.replaceAll("  ", " ");
        String[] splitted = s.split("\n");
        for (String s1 : splitted) {
            if (s1.contains(fixedMac) && !s1.contains("mac")) {
                String[] spl = s1.split(" ");
                return Integer.parseInt(spl[3]);
            }
        }
        return null;
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        sendCommand("sh port 22\n");
        String s = waitForStringWithContinue(">","#");
        String[] splitted = s.split("\n");
        ArrayList<String> lines = new ArrayList<>();

        for (String s1 : splitted) {
            if (s1.contains("PortId") || s1.contains("PortEnable")) {
                s1 = s1.substring(s1.indexOf(":") + 2);
                int index = s1.indexOf(" ");
                if (index == -1) {
                    index = s1.indexOf("\r");
                }
                s1 = s1.substring(0, index);
                lines.add(s1);
            }
        }

        ArrayList<String> combined = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            combined.add(lines.get(i) + " " + lines.get(i + 1));
            i++;
        }

        ArrayList<Integer> downed = new ArrayList<>();
        for (String line : combined) {
            if (line.contains("disabled")) {
                downed.add(Integer.parseInt(line.substring(0, line.indexOf(" "))));
            }
        }
        return downed;
    }

    @Override
    public void login() throws Exception {
        waitForString("login:");
        sendCommand(LOGIN + "\n");
        waitForString("password:");
        sendCommand(PASS + "\n");
        waitForString(">");
        sendCommand("en\n");
        waitForString("password:");
        sendCommand("rbnfqcrbqbynthyt\n");
        waitForString("#");
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        sendCommand("sh fdb port " + port + " de\n");
        String s = waitForString(">", "#");
        ArrayList<String> macs = new ArrayList<>();
        Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{2}\\.){5}[0-9A-Fa-f]{2}").matcher(s);
        while (regexMatcher.find()) {
            macs.add(regexMatcher.group().replaceAll("\\.", ""));
        }
        return macs;
    }
}
