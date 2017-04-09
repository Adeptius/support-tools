package adeptius.telnet;


import adeptius.exceptions.UnknownSwitchException;
import adeptius.swich.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("Duplicates")
public class TelnetClient {

    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    public Swich swich;

    public TelnetClient(String ip) throws Exception {
        socket = new Socket(ip, 23);
        socket.setKeepAlive(true);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        swich = determineType();
        System.out.println("Это " + swich);
        swich.login();
    }

    public static void main(String[] args) throws Exception {
//        TelnetClient client = new TelnetClient("172.18.22.21"); // LinkSys
//        TelnetClient client = new TelnetClient("172.18.80.51");  // OldZTE
//        TelnetClient client = new TelnetClient("172.17.2.103");  // newZTE
//        TelnetClient client = new TelnetClient("172.16.3.16");  // dlink
//        TelnetClient client = new TelnetClient("172.18.201.11");  // dlink
//        TelnetClient client = new TelnetClient("172.17.16.222");  // dlink
//        TelnetClient client = new TelnetClient("172.22.106.12");  // foxgate
//        TelnetClient client = new TelnetClient("172.17.245.22");  // dlink 18
//        TelnetClient client = new TelnetClient("172.22.88.22");  // raisecom   enable (пароль zxr10 или raisecom)
//        TelnetClient client = new TelnetClient("172.18.12.122");  // edge-core
//        TelnetClient client = new TelnetClient("172.22.106.61");  // линксис но не определяется
//        TelnetClient client = new TelnetClient("172.21.26.2");  // рейском но не определяется
//        TelnetClient client = new TelnetClient("172.17.1.33");
//        TelnetClient client = new TelnetClient("172.18.34.12"); // рейском
//        TelnetClient client = new TelnetClient("172.21.24.30");
//        TelnetClient client = new TelnetClient("172.22.106.41"); // foxgate неизвестный
//        TelnetClient client = new TelnetClient("172.18.192.21"); // raisecom
//        TelnetClient client = new TelnetClient("172.18.192.11"); // dlink
        TelnetClient client = new TelnetClient("172.18.194.121");
        client.run();
    }


    public void disconnect() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }

    private void run() throws Exception {

//        List<String> macs = swich.getMacAddress(1);
//        for (String mac : macs) {
//            System.out.println(mac);
//        }

//        System.out.println(swich.getNumbersOfMacsOnPort(25));
//            System.out.println("Port " + i + " macs: " + macs.size());
//        }

//        List<String> macs = swich.getMacAddress(1);
//        for (String mac : macs) {
//            System.out.println("----"+mac+"----");
//        }

        List<Integer> downed = swich.getDownedPorts();
        System.out.println();
        for (Integer integer : downed) {
            System.out.println(integer);
        }

//        int port = swich.findMac("001320125de1");
//        System.out.println(port);

//        swich.makeDhcpOnPort(2);
//        swich.makeStaticOnPort(2);
    }

    private Swich determineType() throws Exception {

        int[] buf = new int[20];
        for (int i = 0; i < buf.length; i++) {
                buf[i] = inputStream.read();
            if (inputStream.available() == 0){
                break;
//                outputStream.write((Swich.LOGIN+"\n").getBytes());
            }
        }
        if (
                Arrays.equals(buf, new int[]{255, 251, 1, 255, 251, 3, 32, 13, 10, 13, 10, 32, 32, 32, 32, 42, 42, 42, 42, 42}) ||
                Arrays.equals(buf, new int[]{32,32,84,111,111,32,109,97,110,121,32,116,101,108,110,101,116,32,99,108})
                ) {
            System.out.println("new ZTE");
            return new NewZte(outputStream, inputStream);

        } else if (
                Arrays.equals(buf, new int[]{255, 253, 3, 255, 251, 3, 255, 253, 1, 255, 251, 1, 13, 10, 13, 13, 10, 85, 115, 101})
                ) {
            System.out.println("Linksys");
            return new Linksys(outputStream, inputStream);

        } else if (
//                Arrays.equals(buf, new int[]{255, 251, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}) ||
                        Arrays.equals(buf, new int[]{84,101,108,110,101,116,32,99,108,105,101,110,116,32,116,111,111,32,109,117})
                || Arrays.equals(buf, new int[]{255, 251, 1, 255, 251, 3, 13, 10, 13, 10, 87, 101, 108, 99, 111, 109, 101, 32, 33, 13})) {
            System.out.println("old Zte");
            return new OldZte(outputStream, inputStream);

        } else if (Arrays.equals(buf, new int[]{255, 253, 3, 255, 251, 3, 255, 251, 1, 27, 91, 48, 109, 27, 91, 50, 74, 27, 91, 49})) {
            System.out.println("dlink18");

        } else if (Arrays.equals(buf, new int[]{255, 253, 3, 255, 251, 3, 255, 251, 1, 27, 91, 48, 109, 27, 91, 49, 59, 49, 72, 27})) {
            System.out.println("dlink");
            return new Dlink(outputStream, inputStream);

        } else if (Arrays.equals(buf, new int[]{255, 251, 1, 27, 91, 50, 74, 27, 91, 49, 59, 49, 72, 13, 10, 85, 115, 101, 114, 110})) {
            System.out.println("foxgate");
            return new FoxGate(outputStream, inputStream);
        } else if (
                   Arrays.equals(buf, new int[]{255,251,1,13,10,13,10,76,111,103,105,110,58,13,10,37,32,65,117,116})
                || Arrays.equals(buf, new int[]{255,251,1,13,10,13,10,76,111,103,105,110,58,87,97,107,101,32,117,112})
                || Arrays.equals(buf, new int[]{255,251,1,13,10,13,10,76,111,103,105,110,58,97,100,101,112,116,13,10})
                ) {
            System.out.println("raisecom");
            return new RaiseCom(outputStream, inputStream);
        } else if (
                Arrays.equals(buf, new int[]{255, 251, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}) ||
                Arrays.equals(buf, new int[]{255, 253, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
//                Arrays.equals(buf, new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0})
                ){
            int c;
            String s = "";
            for (int i = 0; i < 20; i++) {
                char ch = (char) inputStream.read();
                System.out.print(ch);
                s += ((char) ch);
                if (s.contains("*****")){
                    return new NewZte(outputStream, inputStream);
                }else if (s.contains("Welcome !")){
                    return new OldZte(outputStream, inputStream);
                }else if (s.equals("\r\n\r\n")){
                    return new RaiseCom(outputStream, inputStream);
                }else if (s.equals("\u001B[2J\u001B[1;1H\r")){
                    return new FoxGate(outputStream, inputStream);
                }else if (s.equals("ÿû\u0003ÿû\u0001\u001B[0m\u001B[")){
                    return new Dlink(outputStream, inputStream);
                }
            }
        }
        throw new UnknownSwitchException();
    }
}
