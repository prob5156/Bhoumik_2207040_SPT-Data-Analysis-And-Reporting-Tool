package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class DashboardController {

    public void createNewBorehole(ActionEvent e) {
        goTo("rawdata-view.fxml", e);
    }

    public void loadSelected(ActionEvent e) {
        goTo("rawdata-view.fxml", e);
    }

    public void deleteSelected(ActionEvent e) {
    }

    public void viewReport(ActionEvent e) {
        goTo("analysis-view.fxml", e);
    }

    private void goTo(String fxml, ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((javafx.scene.Node)e.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
