package com.example.sptdataanalysisandreportingtool;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardController {

    public void createNewBorehole(ActionEvent e) {
        go("rawdata-view.fxml", e);
    }

    public void viewReport(ActionEvent e) {
        go("analysis-view.fxml", e);
    }

    private void go(String fxml, ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/" + fxml)
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
