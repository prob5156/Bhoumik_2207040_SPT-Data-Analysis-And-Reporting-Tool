package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class SeniorLoginController {

    @FXML private TextField tfUser;
    @FXML private PasswordField tfPass;

    @FXML
    private void login() {
        String u = tfUser.getText();
        String p = tfPass.getText();

        if (!u.isEmpty() && !p.isEmpty()) {
            Session.role = "SENIOR";
            try {
                FXMLLoader f = new FXMLLoader(
                        getClass().getResource("modifiers-dashboard-view.fxml")
                );
                Stage s = (Stage) tfUser.getScene().getWindow();
                Scene sc = new Scene(f.load());
                sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                s.setScene(sc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void back(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("login-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
