package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class HelloController {

    @FXML
    private TextField tfUser;

    @FXML
    private PasswordField pfPass;

    @FXML
    private Label lblError;

    @FXML
    private void login() {
        String u = tfUser.getText();
        String p = pfPass.getText();

        if (u.equals("sub") && p.equals("123")) {
            Session.role = "Sub-Conductor Engineer";
            openDashboard();
        } else if (u.equals("senior") && p.equals("123")) {
            Session.role = "Senior Engineer";
            openDashboard();
        } else {
            lblError.setText("Invalid username or password");
        }
    }

    private void openDashboard() {
        try {
            Stage stage = (Stage) tfUser.getScene().getWindow();
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/dashboard-view.fxml")
            );
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
