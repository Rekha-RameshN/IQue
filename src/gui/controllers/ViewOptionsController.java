package gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import process.QuestionHandler;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class ViewOptionsController implements Initializable {

    public Stage optionsStage;
    QuestionHandler qh;
    int loPartial, qPartial;

    public void setPartials(int loPartial, int qPartial) {
        this.loPartial = loPartial;
        this.qPartial = qPartial;
    }

    public void setQh(QuestionHandler qh) {
        this.qh = qh;
    }

    public void setStage(Stage stage) {
        this.optionsStage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void openIAO() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/view.fxml"));
        Parent root = loader.load();
        ViewController viewController = loader.getController();
        Stage viewStage = new Stage();
        viewStage.setScene(new Scene(root));
        viewController.setStage(viewStage);
        viewController.setQuestionHandler(qh);
        viewController.setPartialState(loPartial, qPartial);
        optionsStage.close();
    }

    public void openReport() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/qualityReport.fxml"));
        Parent root = loader.load();
        QualityReportController controller = loader.getController();
        Stage qualityReportStage = new Stage();        
        qualityReportStage.setTitle("Quality Report");
        controller.setQh(qh);
        controller.loadReport();
        qualityReportStage.setScene(new Scene(root));
        qualityReportStage.show();
        optionsStage.close();
    }
}
