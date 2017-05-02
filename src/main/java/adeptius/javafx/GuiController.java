package adeptius.javafx;


import adeptius.dao.VpsDao;
import adeptius.exceptions.FunctionNotSupportedException;
import adeptius.exceptions.SimultaneousConfigException;
import adeptius.exceptions.UnknownSwitchException;
import adeptius.host.PauseChecker;
import adeptius.host.PauseState;
import adeptius.host.PauseWatcher;
import adeptius.swich.Bdcom;
import adeptius.swich.Linksys;
import adeptius.telnet.TelnetClient;
import adeptius.utilites.StringUtils;
import adeptius.utilites.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static adeptius.utilites.StringUtils.*;

@SuppressWarnings("Duplicates")
public class GuiController implements Initializable {

    public static GuiController guiController;

    public GuiController() {
        this.guiController = this;
    }

    @FXML
    private TextArea findMacSwitchesField;

    @FXML
    private TextArea shortLogArea;

    @FXML
    private TextArea fullLogArea;

    @FXML
    private TextArea findMacResultArea;

    @FXML
    private TextField findMacMacField;

    @FXML
    private TextArea findMacFilterArea;

    @FXML
    private TextArea downPortsSwitchesList;

    @FXML
    private TextArea downPortsResultArea;

    @FXML
    private TextArea feedbackFutureList;




    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<String> list = serversAndIp.keySet().stream().collect(Collectors.toCollection(ArrayList::new));
        dhcpServerList.setItems(FXCollections.observableArrayList(list));
        dhcpServerList.setOnMouseClicked(event -> dhcpLabel.setText(
                StringUtils.convertArrToString(serversAndIp.get(dhcpServerList.getSelectionModel().getSelectedItem()))));
        dhcpStopButton.setDisable(true);
        dosStopButton.setVisible(false);
        try {
            String future = VpsDao.getValue("futureList").replaceAll("\\|", "\n");
            feedbackFutureList.setText(future);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        watchPauseButton.setVisible(false);
//        notifyIfCanPauseButton.setVisible(false);

        new PauseWatcher(this);
    }

    @FXML
    private Button findDownPortsStartButton;
    @FXML
    private Button findMacButton;
    @FXML
    private Button findMacNumberMacButton;
    @FXML
    private Button doDynamicButton;
    @FXML
    private Button doStaticButton;


    private void hideButtons() {
        findDownPortsStartButton.setVisible(false);
        findMacButton.setVisible(false);
        findMacNumberMacButton.setVisible(false);
        doDynamicButton.setVisible(false);
        doStaticButton.setVisible(false);
        findMacBdcomButton.setVisible(false);
    }

    private void showButtons() {
        findDownPortsStartButton.setVisible(true);
        findMacButton.setVisible(true);
        findMacNumberMacButton.setVisible(true);
        doDynamicButton.setVisible(true);
        doStaticButton.setVisible(true);
        findMacBdcomButton.setVisible(true);
    }

    public static void print(String s) {
        Platform.runLater(() -> {
            GuiController.guiController.fullLogArea.appendText(s);
            GuiController.guiController.fullLogArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    @FXML
    void findMac(ActionEvent event) {
        new Thread(() -> {
            completed = 0;
            fullLogArea.setText("");
            shortLogArea.setText("");
            findMacResultArea.setText("");
            findMacFilterArea.setText("");

            String mac = cleanMac(findMacMacField.getText());
            findMacMacField.setText(mac);
            ArrayList<String> switches = getSwitchesFromString(findMacSwitchesField.getText());
            hideButtons();
            switches.parallelStream().forEach(ip -> {
                try {
                    TelnetClient client = new TelnetClient(ip);
                    Integer port = client.swich.findMac(mac);
                    print(ip + "\n");
                    print(client.swich.result.toString());
                    if (port != null) {
                        if (port < 25) {
                            findMacResultArea.appendText(ip + "-" + port + "-ЗДЕСЬ\n");
                        } else {
                            findMacResultArea.appendText(ip + "-" + port + "\n");
                        }
                    } else {
                        shortLogArea.appendText(ip + " не найдено\n");
                    }
                    client.disconnect();
                } catch (UnknownSwitchException e) {
                    shortLogArea.appendText(ip + " Неизвестный свич\n");
                } catch (Exception e) {
                    shortLogArea.appendText(ip + " СБОЙ\n");
                } finally {
                    completed++;
                    if (completed == switches.size()) {
                        fullLogArea.appendText("---Завершено---");
                        shortLogArea.appendText("---Завершено---");
                        findMacResultArea.appendText("---Завершено---");
                    }
                    showButtons();
                }
            });
        }).start();
    }

    @FXML
    void filterByNumberOfMacsOnPort(ActionEvent event) {
        new Thread(() -> {
            completed = 0;
            fullLogArea.setText("");
            findMacFilterArea.setText("");
            shortLogArea.setText("");
            String[] s = findMacResultArea.getText().split("\n");
            HashMap<String, Integer> map = new HashMap();
            for (String s1 : s) {
                if (!s1.equals("") && !s1.equals("---Завершено---")) {
                    String[] swAndMac = s1.split("-");
                    map.put(swAndMac[0], Integer.parseInt(swAndMac[1]));
                }
            }
            hideButtons();

            map.keySet().parallelStream().forEach(ip -> {
                try {
                    TelnetClient client = new TelnetClient(ip);
                    int port = map.get(ip);
                    int macs = client.swich.getNumbersOfMacsOnPort(port);
                    print(ip + "\n");
                    print(client.swich.result.toString());

                    fullLogArea.setScrollTop(Double.MAX_VALUE);
                    if (macs == 0) {
                        shortLogArea.appendText(ip + " не найдено\n");
                    }
                    if (macs == 1) {
                        findMacFilterArea.appendText(ip + " порт " + port + " - НАЙДЕН!\n");
                    }
                    if (macs > 1) {
                        findMacFilterArea.appendText(ip + " порт " + port + " - маков:" + macs + "\n");
                    }
                    client.disconnect();
                } catch (Exception e) {
                    shortLogArea.appendText(ip + " СБОЙ\n");
                } finally {
                    completed++;
                    if (completed == map.size()) {
                        findMacFilterArea.appendText("---Завершено---");
                        shortLogArea.appendText("---Завершено---");
                    }
                    showButtons();
                }
            });
        }).start();
    }

    public static volatile int completed = 0;

    @FXML
    void FindDownPorts(ActionEvent event) {
        new Thread(() -> {
            ArrayList<String> switches = getSwitchesFromString(downPortsSwitchesList.getText());
            fullLogArea.setText("");
            shortLogArea.setText("");
            downPortsResultArea.setText("");
            completed = 0;
            hideButtons();
            switches.stream().parallel().forEach(ip -> {
                try {
                    TelnetClient client = new TelnetClient(ip);
                    ArrayList<Integer> downed = client.swich.getDownedPorts();
                    print(ip + "\n");
                    print(client.swich.result.toString());

                    for (Integer integer : downed) {
                        downPortsResultArea.appendText(ip + "-" + integer + " down\n");
                    }
                    if (downed.size() == 0) {
                        shortLogArea.appendText(ip + "- всё ап\n");
                    }
                    client.disconnect();
                } catch (Exception e) {
                    shortLogArea.appendText(ip + " СБОЙ\n");
                } finally {
                    completed++;
                    if (completed == switches.size()) {
                        fullLogArea.appendText("---Завершено---");
                        shortLogArea.appendText("---Завершено---");
                        downPortsResultArea.appendText("---Завершено---");
                    }
                    showButtons();
                }
            });
        }).start();
    }

    private HashMap<String, String[]> serversAndIp = Utils.getServersAndIp();
    private static volatile boolean interrupted;

    @FXML
    private ListView<String> dhcpServerList;

    @FXML
    private TextField dhcpMacInput;

    @FXML
    private Button dhcpIpButton;

    @FXML
    private Button dhcpMacButton;

    @FXML
    private Button dhcpStopButton;

    @FXML
    private Label dhcpLabel;

    @FXML
    void dhcpFindByMac(ActionEvent event) {
        dhcpMacInput.setText(StringUtils.cleanMac(dhcpMacInput.getText()));
        find();
    }

    @FXML
    void dhcpFindByIp(ActionEvent event) {
        dhcpMacInput.setText(dhcpMacInput.getText().trim());
        find();
    }

    //Дхцп логи
    @FXML
    void dhcpStop(ActionEvent event) {
        interrupted = true;
    }

    void find() {
        interrupted = false;
        dhcpIpButton.setDisable(true);
        dhcpMacButton.setDisable(true);
        dhcpStopButton.setDisable(false);
        String[] ips = serversAndIp.get(dhcpServerList.getSelectionModel().getSelectedItem());
        if (ips == null) return;
        String filter = dhcpMacInput.getText();
        fullLogArea.setText("");

        for (String ip : ips) {
            new Thread(() -> {
                try {
                    Socket socket = new Socket(ip, 9998);
                    socket.setKeepAlive(true);
                    InputStream inputStream = socket.getInputStream();
                    int c;
                    String s = "";
                    while (!interrupted && (c = inputStream.read()) != -1) {
                        s = s + (char) c;
                        if (s.endsWith("\n")) {
                            if (s.contains(filter) && !s.equals("\n")) {
                                fullLogArea.appendText(s);
                            }
                            s = "";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                } finally {
                    dhcpIpButton.setDisable(false);
                    dhcpMacButton.setDisable(false);
                    dhcpStopButton.setDisable(true);
                }
            }).start();
        }
    }


    // Закидывание статики или динамики
    @FXML
    private TextField switchText;

    @FXML
    private TextField portTest;

    public void doDynamic() {
        String ip = switchText.getText().trim();
        int port = Integer.parseInt(portTest.getText().trim());
        hideButtons();

        new Thread(() -> {
            try {
                TelnetClient client = new TelnetClient(ip);
                if (client.swich instanceof Linksys) {
                    fullLogArea.setText("Ищем привязки в running config, удаляем их, передёргиваем порт. Ждите около 30 сек.\n");
                }
                client.swich.makeDhcpOnPort(port);
                print(ip + "\n");
                print(client.swich.result.toString());
            } catch (SimultaneousConfigException e) {
                shortLogArea.appendText(ip + " СБОЙ Выйди из Enable\n");
            } catch (FunctionNotSupportedException e) {
                shortLogArea.appendText(ip + " СБОЙ Функция не поддерживается\n");
            } catch (Exception e) {
                shortLogArea.appendText(ip + " СБОЙ\n");
            } finally {
                showButtons();
            }
        }).start();
    }


    public void doStatic() {
        String ip = switchText.getText().trim();
        int port = Integer.parseInt(portTest.getText().trim());
        hideButtons();
        new Thread(() -> {
            try {
                TelnetClient client = new TelnetClient(ip);
                client.swich.makeStaticOnPort(port);
                print(ip + "\n");
                print(client.swich.result.toString());
            } catch (SimultaneousConfigException e) {
                shortLogArea.appendText(ip + " СБОЙ Выйди из Enable\n");
            } catch (FunctionNotSupportedException e) {
                shortLogArea.appendText(ip + " СБОЙ Функция не поддерживается\n");
            } catch (Exception e) {
                shortLogArea.appendText(ip + " СБОЙ\n");
            } finally {
                showButtons();
            }
        }).start();
    }


    // Дос
    private Process process;
    @FXML
    private TextField abonIpField;
    @FXML
    private Button dosStopButton;
    @FXML
    private Button dosStartButton;

    @FXML
    public void startDos() {
        dosStartButton.setVisible(false);
        dosStopButton.setVisible(true);
        fullLogArea.setText("");
        new Thread(() -> {
            try {
                String iperfURL = Utils.copyIPerf();
                String ip = abonIpField.getText().trim();
                process = Runtime.getRuntime().exec(iperfURL + " -c " + ip + " -u -P 5 -i 1 -p 5001 -f m -b 100M -t 60");
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                line = input.readLine();
                while (line != null) {
                    line = input.readLine();
                    if (line.startsWith("[SUM]")) {
                        fullLogArea.appendText(line + "\n");
                    }
                }
            } catch (Exception ignored) {

            } finally {
                dosStartButton.setVisible(true);
                dosStopButton.setVisible(false);
            }
        }).start();
    }

    @FXML
    public void stopDos() throws Exception {
        if (process != null) {
            process.destroy();
        }
        dosStartButton.setVisible(true);
        dosStopButton.setVisible(false);
    }


    // Обратная связь:
    @FXML
    private TextArea feedbackTextArea;

    @FXML
    private TextField feedbackNik;

    @FXML
    private Label feedbackResultLabel;

    public void sendFeedback() {
        String text = feedbackTextArea.getText();
        String nick = feedbackNik.getText().trim();
        String result = VpsDao.sendFeedBack(nick, text);
        feedbackResultLabel.setText(result);
    }

    @FXML
    private Button findMacBdcomButton;

//    @FXML
//    private ListView<String> listOfBdComs;
//
//    @FXML
//    private TextField inputBdComField;
//
//
//    public void addBdcomToDB(){
//
//    }
//
//    public void removeFromDb(){
//
//    }


    public void findMacOnBdCom() {
        new Thread(() -> {
            completed = 0;
            fullLogArea.setText("");
            shortLogArea.setText("");
            findMacResultArea.setText("");
            findMacFilterArea.setText("");

            String mac = cleanMac(findMacMacField.getText());
            findMacMacField.setText(mac);
            ArrayList<String> switches = getSwitchesFromString(findMacSwitchesField.getText());
            hideButtons();
            switches.parallelStream().forEach(ip -> {
                try {
                    TelnetClient client = new TelnetClient(ip);
                    if (!(client.swich instanceof Bdcom)) {
                        shortLogArea.appendText(ip + " Это не BDCOM\n");
                        return;
                    }
                    String port = client.swich.findMacBdCom(mac);
                    print(ip + "\n");
                    print(client.swich.result.toString());
                    if (port != null) {
                        findMacResultArea.appendText(ip + "-" + port + "\n");
                    } else {
                        shortLogArea.appendText(ip + " не найдено\n");
                    }
                    client.disconnect();
                } catch (FunctionNotSupportedException | UnknownSwitchException e) {
                    shortLogArea.appendText(ip + " Не BDCOM\n");
                } catch (Exception e) {
                    shortLogArea.appendText(ip + " СБОЙ\n");
                } finally {
                    completed++;
                    if (completed == switches.size()) {
                        fullLogArea.appendText("---Завершено---");
                        shortLogArea.appendText("---Завершено---");
                        findMacResultArea.appendText("---Завершено---");
                    }
                    showButtons();
                }
            });
        }).start();
    }


    @FXML
    private TextField cockieField;

    @FXML
    private Label coockiesStatus;

    @FXML
    public Button watchPauseButton;

    @FXML
    public Button notifyIfCanPauseButton;

    public static volatile boolean needWatchIfOnPause;
    public static volatile boolean needNotifyAvailablePause;

    public static String currentCoockie;

    public void setCoockies() {
        String coockies = cockieField.getText();
        if (coockies == null || !coockies.contains("PHPSESSID=")) {
            Platform.runLater(() -> {
                coockiesStatus.setText("Куки неправильные");
                watchPauseButton.setVisible(false);
                notifyIfCanPauseButton.setVisible(false);
            });
            return;
        }

        coockies = coockies.substring(coockies.indexOf("PHPSESSID=") + 10);
        coockies = coockies.substring(0, coockies.indexOf(";"));

        currentCoockie = coockies;
        try {
            PauseState state = PauseChecker.getState(currentCoockie);
            if (state == PauseState.SESSION_ERROR) {
                Platform.runLater(() -> {
                    coockiesStatus.setText("Куки неправильные");
                    watchPauseButton.setVisible(false);
                    notifyIfCanPauseButton.setVisible(false);
                });

            } else {
                Platform.runLater(() -> {
                    coockiesStatus.setText("Куки рабочие");
                    watchPauseButton.setVisible(true);
                    notifyIfCanPauseButton.setVisible(true);
                });
            }
        } catch (Exception e) {
            coockiesStatus.setText("Ошибка");
            Platform.runLater(() -> {
                watchPauseButton.setVisible(false);
                notifyIfCanPauseButton.setVisible(false);
            });
        }

        watchPauseButton.setOnMouseClicked(event -> {
            if (needWatchIfOnPause){
                needWatchIfOnPause = false;
                Platform.runLater(() -> watchPauseButton.setText("Наблюдать за паузой"));
            }else {
                needWatchIfOnPause = true;
                Platform.runLater(() -> watchPauseButton.setText("Не наблюдать за паузой"));
            }
        });

        notifyIfCanPauseButton.setOnMouseClicked(event -> {
            if (needNotifyAvailablePause){
                needNotifyAvailablePause = false;
                Platform.runLater(() -> notifyIfCanPauseButton.setText("Сообщить о свободном месте"));
            }else {
                needNotifyAvailablePause = true;
                Platform.runLater(() -> notifyIfCanPauseButton.setText("Не сообщать о свободном месте"));
            }
        });
    }

    public void openHostInBrowser() {
        Gui.hostServices.showDocument("http://host.o3.ua/support/");
    }
}
