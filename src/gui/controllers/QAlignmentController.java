package gui.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import process.Question;
import process.QuestionHandler;
import process.TesterSheetGenerator;
import train.FileNames;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class QAlignmentController implements Initializable {

    final public static String file_name = FileNames.currentDir() + FileNames.SEPARATOR + FileNames.TEACHER_FOLDER_NAME
            + FileNames.SEPARATOR + "TESTER.xlsx";
    public TextField a, b;
    public Label statusBar;
    private ArrayList<Question> q, los;
    private QuestionHandler qh;

    public void setFiles(ArrayList<Question> q, ArrayList<Question> los, QuestionHandler qh) {
        this.q = q;
        this.los = los;
        this.qh = qh;
    }

    /**
     * Generate file
     *
     * @throws IOException
     */
    public void nextButtonClicked() throws IOException {
        statusBar.setText("Loading...");
        if (!a.getText().isEmpty() && !b.getText().isEmpty()) {
            try {
                float ALPHA = Float.parseFloat(a.getText());
                float BETA = Float.parseFloat(b.getText());

                TesterSheetGenerator tsg = new TesterSheetGenerator();
                tsg.generateSheet(ALPHA, BETA, q, los, qh, file_name);

                statusBar.setText("Completed.");
                Desktop.getDesktop().open(new File(file_name));
            } catch (IOException | OWLOntologyCreationException | OWLOntologyStorageException e) {
                System.err.println("Tester output:" + e);
                statusBar.setText("Error.");
            }
        }
        //startStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
