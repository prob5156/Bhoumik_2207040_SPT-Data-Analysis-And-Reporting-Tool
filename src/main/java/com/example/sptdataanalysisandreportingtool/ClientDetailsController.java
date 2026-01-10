package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class ClientDetailsController {

    @FXML private TextField tfClientName;
    @FXML private TextField tfPhoneNumber;
    @FXML private javafx.scene.control.PasswordField tfPassword;
    @FXML private Label lblHeader;

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else lblHeader.setText("");
        }
    }

    @FXML
    private void submit(ActionEvent e) {
        // Prevent SUB role from creating new clients
        if ("SUB".equalsIgnoreCase(Session.role)) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Insufficient Permissions");
            alert.setContentText("Subconductor Engineer cannot create new clients.");
            alert.showAndWait();
            return;
        }
        String clientName = tfClientName.getText();
        String phoneNumber = tfPhoneNumber.getText();
        String password = tfPassword.getText();

        if (clientName.isEmpty() || phoneNumber.isEmpty() || password.isEmpty()) {
            return;
        }

        // Store client name in session to pass to next screen
        Session.clientName = clientName;
        Session.phoneNumber = phoneNumber;
        Session.clearLocations();  // Clear any previous locations

        try {
            // persist client and get id
            int id = DBUtil.insertClient(clientName, phoneNumber, password);
            Session.clientId = id;

            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/modifiers-dashboard-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void back(ActionEvent e) {
        try {
                FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/modifiers-dashboard-view.fxml")
                );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
