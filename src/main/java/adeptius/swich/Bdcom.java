package adeptius.swich;

import adeptius.exceptions.FunctionNotSupportedException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Bdcom extends Swich {

    public Bdcom(OutputStream outputStream, InputStream inputStream) {
        super(outputStream, inputStream);
    }

    @Override
    public String findMacBdCom(String mac) throws Exception {
        mac = mac.substring(0, 4) + "."
                + mac.substring(4, 8) + "."
                + mac.substring(8);
        sendCommand("sh mac address-table " +mac+"\n");
        String s = waitForString("Switch#");
        s = s.substring(30);
        s = s.replaceAll("\t", " ");
        s = s.replaceAll("     ", " ");
        s = s.replaceAll("    ", " ");
        s = s.replaceAll("   ", " ");
        s = s.replaceAll("  ", " ");
        s = s.trim();
        String[] splitted = s.split("\n");

        String withMac = null;
        for (String s1 : splitted) {
            Matcher regexMatcher = Pattern.compile("([0-9A-Fa-f]{4}.){2}[0-9A-Fa-f]{4}").matcher(s1);
            if (regexMatcher.find()){
                withMac = s1;
                break;
            }
        }
        if (withMac == null){
            return null;
        }
        return withMac.split(" ")[3];
    }

    @Override
    public void login() throws Exception {
        waitForString("Username:");
        sendCommand("duty\n");
        waitForString("Password:");
        sendCommand("support\n");
        waitForString("Switch>");
        sendCommand("enable\n");
        waitForString("password:");
        sendCommand("bdcom\n");
        waitForString("Switch#");
    }

    @Override
    public ArrayList<String> getMacAddress(int port) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public ArrayList<Integer> getDownedPorts() throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public Integer findMac(String mac) throws Exception {
        throw new FunctionNotSupportedException();
    }

    @Override
    public int getNumbersOfMacsOnPort(int port) throws Exception {
        throw new FunctionNotSupportedException();
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
    public void portUp(int port) throws Exception {
        throw new FunctionNotSupportedException();
   }


}
