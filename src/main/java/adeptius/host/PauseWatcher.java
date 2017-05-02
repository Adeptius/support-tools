package adeptius.host;

import adeptius.javafx.Gui;
import adeptius.javafx.GuiController;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PauseWatcher extends Thread {


    private GuiController guiController;
    private String tempDir = "";

    public PauseWatcher(GuiController guiController) {

        setDaemon(true);
        this.guiController = guiController;
        try {
            tempDir = init();
            start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true){
            try{
                check();
                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void check() throws Exception {

        if (!GuiController.needWatchIfOnPause){
            return;
        }

        PauseState state = PauseChecker.getState(GuiController.currentCoockie);

        System.out.println(state);
        if (state == PauseState.SESSION_ERROR){
            GuiController.needWatchIfOnPause = false;
            GuiController.needNotifyAvailablePause = false;
            Platform.runLater(() -> {
                guiController.watchPauseButton.setText("Наблюдать за паузой");
                guiController.notifyIfCanPauseButton.setText("Сообщить о свободном месте");
            });
            playSound(state);

        }else if (state == PauseState.ON_PAUSE){
            playSound(state);

        }else if (state == PauseState.CAN_STOP){
            if (GuiController.needNotifyAvailablePause){
                playSound(state);
            }
        }
    }


    private void playSound(PauseState state){
        String url = "";

        if (state == PauseState.SESSION_ERROR){
           url = "error.mp3";
        }else if (state == PauseState.ON_PAUSE){
            url = "onPause.mp3";
        }else if (state == PauseState.CAN_STOP){
            url = "canStop.mp3";
        }
        url = tempDir + url;

        MediaPlayer mediaPlayer = new MediaPlayer(new Media(new File(url).toURI().toString()));
        mediaPlayer.setAutoPlay(true);
    }


//    private boolean

    public static String init() throws Exception{
        String tempDir = System.getProperty("java.io.tmpdir");
        Files.copy(Gui.class.getResourceAsStream("error.mp3"), Paths.get(tempDir + "error.mp3"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Gui.class.getResourceAsStream("onPause.mp3"), Paths.get(tempDir + "onPause.mp3"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(Gui.class.getResourceAsStream("canStop.mp3"), Paths.get(tempDir + "canStop.mp3"), StandardCopyOption.REPLACE_EXISTING);
        return tempDir;
    }
}
