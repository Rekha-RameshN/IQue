package gui.controllers;

import static gui.Main_old.los;
import static gui.Main_old.owl;
import static gui.Main_old.q;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * IQuE start screen
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class StartController implements Initializable {

    public Stage startStage, viewStage, editStage;
    public Label statusBar, loFileLabel, qFileLabel, owlFileLabel;
    //public CheckBox editLO, editQuestions;
    public CheckBox isAdmin;

    private String[] previousFiles;
    private FileChooser fileChooser;
    private boolean flagLo, flagQ = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //isAdmin.setVisible(false);
        fileChooser = new FileChooser();
        previousFiles = new String[3];
        previousFiles[0] = "los.txt";
        previousFiles[1] = "q.txt";
        previousFiles[2] = "final.owl";
    }

    public void setStage(Stage stage) {
        this.startStage = stage;
    }

    public void loButtonClicked() throws IOException {
        System.out.println("LO Click");
        fileChooser.setTitle("Open Learning Objective File");
        fileChooser.setInitialDirectory(new File("."));
        los = fileChooser.showOpenDialog(startStage);
        loFileLabel.setText(los.getName());
    }

    public void questionsButtonClicked() {
        System.out.println("Questions Click");
        fileChooser.setTitle("Choose Questions File");
        fileChooser.setInitialDirectory(new File("."));
        q = fileChooser.showOpenDialog(startStage);
        qFileLabel.setText(q.getName());
    }
    /*public void editLoToggle(){
     flagLo = editLO.isSelected();
     System.out.println("Edit LO Toggle to "+flagLo);
     }
     public void editQToggle(){
     flagQ = editQuestions.isSelected();
     System.out.println("Edit Q Toggle to "+flagQ);

     }*/

    public void ontologyButtonClicked() {
        System.out.println("Ontology Click");
        fileChooser.setTitle("Choose Ontology File");
        owl = fileChooser.showOpenDialog(startStage);
        fileChooser.setInitialDirectory(new File("."));
        owlFileLabel.setText(owl.getName());
    }

    public void nextButtonClicked() throws IOException {
        statusBar.setText("Loading...");
        /*if (editLO.isSelected() || editQuestions.isSelected()){
         loadEdit();
         }
         else{
         loadView();
         }*/
        flagLo = true;
        flagQ = true;
        loadEdit();
        startStage.close();

    }

    public void loadPreviousFiles() {
        los = new File(previousFiles[0]);
        q = new File(previousFiles[1]);
        owl = new File(previousFiles[2]);
        loFileLabel.setText(los.getName());
        qFileLabel.setText(q.getName());
        owlFileLabel.setText(owl.getName());
    }

    private void loadEdit() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/edit.fxml"));
        Parent root = loader.load();
        EditController editController = loader.getController();
        editStage = new Stage();
        editStage.setScene(new Scene(root));
        editController.numLabel.setText("Press Next");
        editController.setFlags(flagLo, flagQ);
        editStage.setTitle("IQuE");        
        editStage.setResizable(false);
        editController.setStage(editStage);
        editController.setAdminControls(isAdmin.isSelected());
        editController.callOnlyOnceToLoadStart();
        editStage.show();
    }

    private void loadView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../scenes/view.fxml"));
        Parent root = loader.load();
        ViewController viewController = loader.getController();
        viewStage = new Stage();
        viewStage.setScene(new Scene(root));
        viewController.setStage(viewStage);
    }
}
