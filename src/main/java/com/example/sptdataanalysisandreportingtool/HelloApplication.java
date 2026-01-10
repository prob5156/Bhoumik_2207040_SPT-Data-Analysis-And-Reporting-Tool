package com.example.sptdataanalysisandreportingtool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // initialize database (create tables) before loading UI
        DBUtil.init();

        FXMLLoader f = new FXMLLoader(
            getClass().getResource("login-view.fxml")
        );
        Scene scene = new Scene(f.load());
        // load global stylesheet
        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception ex) {
            System.err.println("Failed to load style.css: " + ex.getMessage());
        }
        stage.setScene(scene);
        stage.setTitle("SPT Data Analysis Tool");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
