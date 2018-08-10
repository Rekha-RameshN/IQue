package gui;

import javafx.application.Application;

/**
 *
 * @author Niraj Palecha <nirajftw@gmail.com>
 */
public class BarChart extends Application {
 
    @Override
    public void start(javafx.stage.Stage primaryStage) {
 
        // path in current directory for WebEngine.load()
        final String html = "webview/index.html";
        final java.net.URI uri = java.nio.file.Paths.get(html).toAbsolutePath().toUri();
 
        // create WebView with specified local content
        final javafx.scene.web.WebView root = new javafx.scene.web.WebView();
        root.getEngine().load(uri.toString());
        root.setZoom(javafx.stage.Screen.getPrimary().getDpi() / 96);
 
        primaryStage.setTitle("Bar Chart | IQuE");
        primaryStage.setScene(new javafx.scene.Scene(root, 1200, 700));
        primaryStage.show();
    }
    
    public static void main(String[] args){
      Application.launch(args);
    }
    
    
}