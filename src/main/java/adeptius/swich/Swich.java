package adeptius.swich;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

@SuppressWarnings("Duplicates")
public abstract class Swich {

    private OutputStream outputStream;
    private InputStream inputStream;
    public static final String LOGIN = "adept";
    public static final String PASS = "fast";


    public Swich(OutputStream outputStream, InputStream inputStream) {
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    abstract public void login() throws Exception;

    abstract public ArrayList<String> getMacAddress(int port) throws Exception;

    abstract public ArrayList<Integer> getDownedPorts() throws Exception;

    abstract public Integer findMac(String mac) throws Exception;

    abstract public int getNumbersOfMacsOnPort(int port) throws Exception;

    abstract public void makeDhcpOnPort(int port) throws Exception;

    abstract public void makeStaticOnPort(int port) throws Exception;

    public StringBuilder result = new StringBuilder();

    protected String waitForString(String awaiting) throws Exception {
        return waitForString(new String[]{awaiting});
//        int c;
//        String s = "";
//        while ((c = inputStream.read()) != -1) {
//            System.err.print((char) c);
//            s += ((char) c);
//            checkForStop(s);
//            if (s.endsWith(awaiting)) {
//                result.append(s);
//                return s;
//            }
//        }
//        return "Error";
    }

    protected String waitForString(String awaiting, String awaiting2) throws Exception {
        return waitForString(new String[]{awaiting,awaiting2});
//        int c;
//        String s = "";
//        while ((c = inputStream.read()) != -1) {
//            System.err.print((char) c);
//            s += ((char) c);
//            checkForStop(s);
//            if (s.endsWith(awaiting) || s.endsWith(awaiting2)) {
//                result.append(s);
//                return s;
//            }
//        }
//        return "Error";
    }

    protected String waitForString(String[] awaiting) throws Exception {
        int c;
        String s = "";
        while ((c = inputStream.read()) != -1) {
            System.err.print((char) c);
            s += ((char) c);
            checkForStop(s);
            for (String s1 : awaiting) {
                if (s.endsWith(s1)){
                    result.append(s);
                    return s;
                }
            }
        }
        return "Error";
    }

    protected String waitForString(String awaiting, String awaiting2, String awaiting3) throws Exception {
        return waitForStringWithContinue(new String[]{awaiting, awaiting2, awaiting3});
//        int c;
//        String s = "";
//        while ((c = inputStream.read()) != -1) {
//            System.err.print((char) c);
//            s += ((char) c);
//            checkForStop(s);
//            if (s.endsWith(awaiting) || s.endsWith(awaiting2) || s.endsWith(awaiting3)) {
//                result.append(s);
//                return s;
//            }
//        }
//        return "Error";
    }

    protected String waitForStringWithContinue(String awaiting, String awaiting2) throws Exception {
        return waitForStringWithContinue(new String[]{awaiting, awaiting2});

//        int c;
//        String s = "";
//        while ((c = inputStream.read()) != -1) {
//            System.err.print((char) c);
//            s += ((char) c);
//            checkForContinue(s);
//            if (s.endsWith(awaiting) || s.endsWith(awaiting2)) {
//                s = s.replaceAll("\n\n\n", "\n");
//                s = s.replaceAll("\n\n\n", "\n");
//                result.append(s);
//                return s;
//            }
//        }
//        return "Error";
    }


    protected String waitForStringWithContinue(String awaiting) throws Exception {
        return waitForStringWithContinue(new String[]{awaiting});
//        int c;
//        String s = "";
//        while ((c = inputStream.read()) != -1) {
//            System.err.print((char) c);
//            s += ((char) c);
//            checkForContinue(s);
//            if (s.endsWith(awaiting)) {
//                s = s.replaceAll("\n\n\n", "\n");
//                s = s.replaceAll("\n\n\n", "\n");
//                result.append(s);
//                return s;
//            }
//        }
//        return "Error";
    }

    protected String waitForStringWithContinue(String[] awaiting) throws Exception {
        int c;
        String s = "";
        while ((c = inputStream.read()) != -1) {
            System.err.print((char) c);
            s += ((char) c);
            checkForContinue(s);
            for (String s1 : awaiting) {
                if (s.endsWith(s1)) {
                    s = s.replaceAll("\n\n\n", "\n");
                    s = s.replaceAll("\n\n\n", "\n");
                    result.append(s);
                    return s;
                }
            }
        }
        return "Error";
    }


    protected void sendCommand(String s) throws Exception {
        s = s.replaceAll("\n", "\r\n");
        outputStream.write(s.getBytes());
    }

    private void checkForStop(String s) throws Exception {
        if (s.endsWith("ress Q")) { // zte
            sendCommand("q\n");
        }
        if (s.endsWith("Quit")) { // linksys
            sendCommand("q\n");
        }
        if (s.endsWith("Q to quit")) { // foxgate
            sendCommand("q\n");
        }
        if (s.endsWith("--More--")) { // RaiseCom
            sendCommand("q\n");
        }
    }

    private void checkForContinue(String s) throws Exception {
        if (s.endsWith("ress Q")) { // zte
            sendCommand(" \n");
        }
        if (s.endsWith("Quit")) { // linksys
            sendCommand(" \n");
        }
        if (s.endsWith("other key to next page")) { // foxgate
            sendCommand(" \n");
        }
        if (s.endsWith("More")) { // raisecom
            sendCommand(" \n");
        }
    }
}
