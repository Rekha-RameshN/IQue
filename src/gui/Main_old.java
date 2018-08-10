package gui;

import gui.controllers.StartController;
import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class Main_old extends Application {

    public static File los, q, owl;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage startStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scenes/start.fxml"));
        Parent root = loader.load();
        StartController controller = loader.getController();
        controller.setStage(startStage);
        startStage.setTitle("IQuE");
        startStage.setScene(new Scene(root));
        startStage.show();
    }

}
