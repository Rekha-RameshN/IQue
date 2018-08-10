package gui.controllers;

import gui.Main_old;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import process.Question;
import process.QuestionHandler;

/**
 * edit.FXML Controller class
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class EditController implements Initializable {

    public Stage editStage, viewStage, qAlignStage, conMatStage, viewOptionsStage;
    public Label numLabel, skipWarning;
    public int current = 0, lorq = -1, currSize = 0;
    public Button nextButton, backButton, viewButton, exitButton, skipButton;
    public TextField skipText, levelText, slotIndicator, slotConnector, relationText;
    public TextArea conceptText, questionText;
    public HBox testerControls, slotBox;
    public int loPartial = -1, qPartial = -1;
    //private boolean viewOpen = false;
    private ViewController viewController;

    private QuestionHandler qh;
    private boolean flagLo, flagQ;
    private int losSize, qSize;

    private boolean calledOnStart = false, isAdmin = false;

    public void setStage(Stage stage) {
        editStage = stage;
    }

    public void setFlags(boolean flagLo, boolean flagQ) {
        this.flagLo = flagLo;
        this.flagQ = flagQ;
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            qh = new QuestionHandler(Main_old.los, Main_old.q, Main_old.owl);
            losSize = qh.losSize;
            qSize = qh.qSize;
        } catch (OWLOntologyCreationException | IOException | OWLOntologyStorageException ex) {
            Logger.getLogger(EditController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setAdminControls(boolean status) {
        isAdmin=status;
        testerControls.setVisible(status);
        slotBox.setVisible(status);
    }

    public void callOnlyOnceToLoadStart() {
        if (!calledOnStart) {
            nextClicked();
            nextClicked();
            calledOnStart = true;
        }
    }

    private void loadNext() {
        if (lorq == 0) {
            Question tmp = qh.los.get(current - 1);
            loPartial = current - 1;
            loadQuestion(tmp);
            if (current == 1) {
                backButton.setDisable(true);
            } else {
                backButton.setDisable(false);
            }
            nextButton.setDisable(false);
        } else {
            Question tmp = qh.questions.get(current - 1);
            qPartial = current - 1;
            loadQuestion(tmp);
            if (current == qSize) {
                nextButton.setDisable(true);
            } else {
                nextButton.setDisable(false);
            }
            backButton.setDisable(false);
        }
    }

    private void loadQuestion(Question question) {
        questionText.setText(question.getStatement());
        levelText.setText(question.getLevelName());
        if (question.getSlot()) {
            slotConnector.setText(String.valueOf(question.getSlotConnector()));
            slotIndicator.setText(String.valueOf(question.getSlotIndicator()));
        } else {
            slotConnector.setText("");
            slotIndicator.setText("");
        }
        relationText.setText(question.getRelation().keySet().toString());

        String conceptString = question.getImplicitConcepts().toString();
        if (isAdmin) {
            conceptString+="\n\nCLASSIFICATION:\n"+question.getClassifyConcepts();
        }
        conceptText.setText(conceptString);
        

    }

    public void backClicked() {
        if (current > 1 && lorq != -1) {
            current -= 1;
            setLabel(current);
            loadNext();
            //nextButton.setDisable(false);
        } else if (current == 1 && lorq == 1 && flagLo) {
            current = losSize;
            currSize = losSize;
            skipWarning.setText("Total statements:" + currSize);
            lorq = 0;
            setLabel(current);
            loadNext();
        } else {
            //backButton.setDisable(true);
        }
    }

    public void viewClicked() throws IOException {
        if (viewOptionsStage == null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/viewOptions.fxml"));
            Parent root = loader.load();
            ViewOptionsController vo = loader.getController();
            viewOptionsStage = new Stage();
            viewOptionsStage.setScene(new Scene(root));
            viewOptionsStage.setResizable(false);
            vo.setStage(viewOptionsStage);
            vo.setQh(qh);
            vo.setPartials(loPartial, qPartial);
        }
        viewOptionsStage.show();
//            exitButton.setDisable(true);
        /*if (!viewOpen) {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/view.fxml"));
         Parent root = loader.load();
         viewController = loader.getController();
         viewStage = new Stage();
         viewStage.setScene(new Scene(root));
         viewController.setStage(viewStage);
         viewController.setQuestionHandler(qh);
         viewController.setPartialState(loPartial, qPartial);
         viewOpen = true;
         } else {
         viewController.setPartialState(loPartial, qPartial);
         }*/
//		viewStage.setX(0);
//		viewStage.setY(0);
//		viewStage.setMaxWidth(586);
//		viewStage.setMaximized(true);

    }

    public void nextClicked() {
        // System.out.println(flagLo+" "+flagQ+" "+lorq+" "+current);
        if (current < currSize && lorq != -1) {
            current += 1;
            setLabel(current);
            loadNext();
            //backButton.setDisable(false);
        } else if (lorq == -1) {
            if (flagLo) {
                currSize = losSize;
                skipWarning.setText("Total statements:" + currSize);
                lorq = 0;
            } else {
                currSize = qSize;
                skipWarning.setText("Total statements:" + currSize);
                lorq = 1;
            }
        } else {
            if (lorq == 0 && flagQ) {
                lorq = 1;
                current = 0;
                currSize = qSize;
                skipWarning.setText("Total statements:" + currSize);
                current += 1;
                setLabel(current);
                loadNext();
            } else {
                //nextButton.setDisable(true);
            }
        }
    }

    public void skipButtonClicked() {
        int skipNum = Integer.parseInt(skipText.getText());
        if (skipNum <= currSize) {
            current = skipNum;
            setLabel(current);
            loadNext();
        } else {
            skipWarning.setText("Total statements:" + currSize);
        }
        //System.out.println(skipNum + " " + currSize);        
    }

    private void setLabel(int current) {
        if (lorq == 0) {
            numLabel.setText("Learning Objective #" + String.valueOf(current));
        } else {
            numLabel.setText("Question #" + String.valueOf(current));
        }
    }

    public void exitClicked() {
        System.exit(0);
    }

    public void openQAlignment() {
        try {
            if (qAlignStage == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/qAlignment.fxml"));
                Parent root = loader.load();
                QAlignmentController qController = loader.getController();
                qAlignStage = new Stage();
                qAlignStage.setScene(new Scene(root));
                qAlignStage.setResizable(false);
                qController.setFiles(qh.questions, qh.los, qh);
            }
            qAlignStage.show();
        } catch (Exception e) {
            System.err.println("" + e);
        }
    }

    public void openConMat() {
        try {
            if (conMatStage == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/confusionMatrix.fxml"));
                Parent root = loader.load();
                ConfusionMatrixController qController = loader.getController();
                conMatStage = new Stage();
                conMatStage.setScene(new Scene(root));
                conMatStage.setResizable(false);
                qController.setFiles(qh.questions, qh.los, qh, conMatStage);
            }
            conMatStage.show();
        } catch (Exception e) {
            System.err.println("" + e);
        }
    }
}
