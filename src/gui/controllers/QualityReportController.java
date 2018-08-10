package gui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import process.QuestionHandler;
import process.TesterSheetGenerator;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class QualityReportController implements Initializable {

    public Label feedbackArea;

    QuestionHandler qh;

    public void setQh(QuestionHandler qh) {
        this.qh = qh;
    }

    public void loadReport() {
        try {
            TesterSheetGenerator tsg = new TesterSheetGenerator();
            ArrayList x = tsg.generateAlignmentValues(qh.questions, qh.los, qh);
            double contentAlignment = (double) x.get(0),
                    cogLevelAlignment = (double) x.get(1);
            ArrayList<Integer> nonUtilityQ = (ArrayList<Integer>) x.get(2);

            String feed = "Content Alignment:" + contentAlignment + "%\n\n"
                    +"Cognitive Level Alignment:" + cogLevelAlignment + "%\n\n";

            if (nonUtilityQ.size() > 0) {
                feed += "The Questions which do not affect the above values:\n" + nonUtilityQ + "\n";
            }
            feedbackArea.setText(feed);
        } catch (IOException e) {
            System.err.println("Load Report:" + e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
