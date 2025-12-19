package com.example.sptdataanalysisandreportingtool;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField tfUser;
    @FXML private PasswordField pfPass;
    @FXML private Label lblError;

    public void login(ActionEvent e) {
        try {
            String u = tfUser.getText();
            String p = pfPass.getText();

            if (u.equals("sub") && p.equals("1234")) {
                Session.username = "Sub-Conductor Engineer";
                Session.role = "SUB";
                loadDashboard(e);
            }
            else if (u.equals("senior") && p.equals("9999")) {
                Session.username = "Senior Engineer";
                Session.role = "SENIOR";
                loadDashboard(e);
            }
            else {
                lblError.setText("Invalid username or password");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadDashboard(ActionEvent e) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/sptdataanalysisandreportingtool/dashboard-view.fxml")
        );
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }
}
