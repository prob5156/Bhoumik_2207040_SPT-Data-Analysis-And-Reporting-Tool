package com.example.sptdataanalysisandreportingtool;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private Label lblRole;

    @FXML
    public void initialize() {
        lblRole.setText("Logged in as: " + Session.role);
    }

    @FXML
    private void createNewBorehole(ActionEvent e) {
        loadScene("rawdata-view.fxml", e);
    }

    @FXML
    private void viewReport(ActionEvent e) {
        loadScene("analysis-view.fxml", e);
    }

    @FXML
    private void logout(ActionEvent e) {
        Session.role = "";
        loadScene("login-view.fxml", e);
    }

    private void loadScene(String fxml, ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/" + fxml)
            );
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
