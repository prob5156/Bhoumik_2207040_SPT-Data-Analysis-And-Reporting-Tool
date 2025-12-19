package com.example.sptdataanalysisandreportingtool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Scene scene = new Scene(
                    new FXMLLoader(
                            getClass().getResource("/com/example/sptdataanalysisandreportingtool/login-view.fxml")
                    ).load()
            );
            stage.setTitle("SPT Data Analysis and Reporting Tool");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
