package adeptius.javafx;


import adeptius.dao.VpsDao;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Gui extends Application {

    public static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Support");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        primaryStage.getIcons().add(new Image(Gui.class.getResourceAsStream( "adeptius64.png" )));
        primaryStage.show();

        hostServices = getHostServices();

        if (!VpsDao.getValue("enabled").equals("true")){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("alert.fxml"));
                Stage stage = new Stage();
                stage.setOnCloseRequest(e -> Platform.exit());
                Parent root2 = loader.load();
                Scene scene = new Scene(root2);
                stage.setTitle("Тестовый период");
            stage.initModality(Modality.WINDOW_MODAL); // Перекрывающее окно
                stage.initOwner(primaryStage); // Указание кого оно перекрывает
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int availableVerdion = Integer.parseInt(VpsDao.getValue("latestVersion"));
        if (availableVerdion>VERSION){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("update.fxml"));
                Stage stage = new Stage();
//                stage.setOnCloseRequest(e -> Platform.exit());
                Parent root2 = loader.load();
                Scene scene = new Scene(root2);
                stage.setTitle("Обновление");
                stage.initModality(Modality.WINDOW_MODAL); // Перекрывающее окно
                stage.initOwner(primaryStage); // Указание кого оно перекрывает
                stage.setScene(scene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final int VERSION = 6;

    public void startGui(){
        launch();
    }

    public static void main(String[] args) {
        Gui gui = new Gui();
        gui.startGui();
    }
}
