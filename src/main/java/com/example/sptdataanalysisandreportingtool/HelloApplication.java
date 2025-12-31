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
        stage.setScene(new Scene(f.load()));
        stage.setTitle("SPT Data Analysis Tool");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
