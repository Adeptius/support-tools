package adeptius.javafx;

import adeptius.dao.VpsDao;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;


public class UpdateController implements Initializable {

    @FXML
    private TextArea releaseNotesArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            String desc = VpsDao.getValue("latestDesc");
            desc = desc.replaceAll("\\|","\n");
            releaseNotesArea.setText(desc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update() {
        String url = "http://195.181.208.31/web/support/downloadNewest";
        Gui.hostServices.showDocument(url);
        Platform.exit();
    }
}
