package gui.controllers;

//import gui.TesterCompareLevel2;
//import gui.TesterCompareLevel3;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import process.Question;
import process.QuestionHandler;
import process.TesterSheetGenerator;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class ConfusionMatrixController implements Initializable {

    File expertFile;
    Stage stage;
    public ComboBox expertSheetName;
    public ToggleGroup sheetType, levelType;

    private ArrayList<Question> q, los;
    private QuestionHandler qh;
    boolean formula = true;
    boolean l2 = true;

    public void setFiles(ArrayList<Question> q, ArrayList<Question> los, QuestionHandler qh, Stage stage) {
        this.q = q;
        this.los = los;
        this.qh = qh;
        this.stage = stage;
    }

    public Label expertFileLabel;

    /**
     * Set expert file
     */
    public void openExpertFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Expert File");
        fileChooser.setInitialDirectory(new File("."));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel Sheet", "*.xlsx");
        fileChooser.getExtensionFilters().add(extFilter);
        expertFile = fileChooser.showOpenDialog(stage);
        if (expertFile != null && expertFile.exists()) {
            try {
                XSSFWorkbook wb = new XSSFWorkbook(expertFile);
                ArrayList<String> sheetNames = new ArrayList<>();
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    sheetNames.add(wb.getSheetName(i));
                }
                expertSheetName.getItems().clear();
                expertSheetName.getItems().addAll(sheetNames);
                wb.close();
            } catch (Exception e) {
                System.err.println("" + e);
            }
            expertFileLabel.setText(expertFile.getName());
        }
    }
//
//    public void onGenerate() {
//        String sheetName = expertSheetName.getValue().toString();
//        if (!sheetName.equals("")) {
//            try {
//                TesterSheetGenerator tsg = new TesterSheetGenerator();
//                tsg.generateSheet(q, los, qh, sheetName);
//
//                if (l2) {
//                    TesterCompareLevel2 t = new TesterCompareLevel2();
//                    if (formula) {
//                        t.compareResult(expertFile.toString(), sheetName, QAlignmentController.file_name, TesterSheetGenerator.sheetName4);
//                    } else {
//                        t.compareCog(expertFile.toString(), sheetName, QAlignmentController.file_name, TesterSheetGenerator.sheetName1);
//                    }
//                } else {
//                    TesterCompareLevel3 t = new TesterCompareLevel3();
//                    if (formula) {
//                        t.compareResult(expertFile.toString(), sheetName, QAlignmentController.file_name, TesterSheetGenerator.sheetName4);
//                    } else {
//                        t.compareCog(expertFile.toString(), sheetName, QAlignmentController.file_name, TesterSheetGenerator.sheetName1);
//                    }
//                }
//            } catch (Exception e) {
//                System.err.println("" + e);
//            }
//        }
//    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        expertSheetName.getItems().clear();
        sheetType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                RadioButton chk = (RadioButton) t1.getToggleGroup().getSelectedToggle();
                if (chk.getText().equalsIgnoreCase("formula")) {
                    formula = true;
                } else {
                    formula = false;
                }
            }
        });
        levelType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                RadioButton chk = (RadioButton) t1.getToggleGroup().getSelectedToggle();
                if (chk.getText().equalsIgnoreCase("2")) {
                    l2 = true;
                } else {
                    l2 = false;
                }
            }
        });
    }

}
