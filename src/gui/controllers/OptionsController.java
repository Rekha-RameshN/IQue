package gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Niraj
 */
public class OptionsController implements Initializable {

    //public static Button train, test, ique;
    public Stage optionsStage,trainStage,testStage,iqueStage;

    public void setStage(Stage stage) {
        this.optionsStage = stage;        
    }

    /**
     * Initializes the controller class.
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {                
    }

    public void openIQuE() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/start.fxml"));
        Parent root = loader.load();
        StartController controller = loader.getController();
        iqueStage = new Stage();
        controller.setStage(iqueStage);
        iqueStage.setTitle("IQuE");
        iqueStage.setScene(new Scene(root));
        iqueStage.setResizable(false);
        iqueStage.show();
        optionsStage.close();
    }

}
