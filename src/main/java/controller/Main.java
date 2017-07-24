package controller;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage = null;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("/view/sample.fxml"));
        this.primaryStage.setTitle("VideoDissector");
        this.primaryStage.setScene(new Scene(root));
        this.primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage getPrimaryStage(){
        return primaryStage;
    }
}
