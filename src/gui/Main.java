package gui;

import gui.controllers.OptionsController;
import java.awt.event.ActionEvent;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage startStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/options.fxml"));
        Parent root = loader.load();
        OptionsController controller = loader.getController();        
        startStage.setTitle("Select");
        startStage.setResizable(false);
        Scene scene = new Scene(root);
        startStage.setScene(scene);
        controller.setStage(startStage);
        startStage.show();       
    }
}
