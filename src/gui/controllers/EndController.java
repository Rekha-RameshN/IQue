package gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 *
 * @author Akshen Kadakia <akshenk8@gmail.com>
 */
public class EndController implements Initializable{
    Stage stage3;
    
    public void setStage(Stage s){
        stage3=s;
        /*new Thread(){

            @Override
            public void run() {
                try{
                    Thread.sleep(500);
                    stage3.close();
                }
                catch(Exception e){ 
                    System.out.println("end:"+e);
                }
            }
            
        }.start();*/
    }    

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
}
