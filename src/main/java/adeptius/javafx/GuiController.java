package adeptius.javafx;


import adeptius.exceptions.FunctionNotSupportedException;
import adeptius.exceptions.SimultaneousConfigException;
import adeptius.exceptions.UnknownSwitchException;
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

import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static adeptius.utilites.StringUtils.*;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<String> list = serversAndIp.keySet().stream().collect(Collectors.toCollection(ArrayList::new));
        dhcpServerList.setItems(FXCollections.observableArrayList(list));
        dhcpServerList.setOnMouseClicked(event -> dhcpLabel.setText(
                StringUtils.convertArrToString(serversAndIp.get(dhcpServerList.getSelectionModel().getSelectedItem()))));
        dhcpStopButton.setDisable(true);
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

    private void clearResults() {

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
                if (client.swich instanceof Linksys){
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
            }finally {
                showButtons();
            }
        }).start();
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
    }

    private void showButtons() {
        findDownPortsStartButton.setVisible(true);
        findMacButton.setVisible(true);
        findMacNumberMacButton.setVisible(true);
        doDynamicButton.setVisible(true);
        doStaticButton.setVisible(true);
    }

    @FXML
    void dhcpStop(ActionEvent event) {
        interrupted = true;
    }
}
