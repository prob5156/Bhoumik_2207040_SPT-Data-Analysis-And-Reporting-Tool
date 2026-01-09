package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class EditClientController {

    @FXML private TextField txfClientName;
    @FXML private TextField txfPhoneNumber;
    @FXML private javafx.scene.control.PasswordField txfPassword;

    private int clientId;

    @FXML
    public void initialize() {
        // Pre-fill with current client data from Session
        clientId = Session.clientId;
        txfClientName.setText(Session.clientName);
        txfPhoneNumber.setText(Session.phoneNumber);
        txfPassword.setText(Session.clientPassword);
    }

    @FXML
    private void save(ActionEvent e) {
        // Prevent SUB role from saving client edits
        if ("SUB".equalsIgnoreCase(Session.role)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Insufficient Permissions");
            alert.setContentText("Subconductor Engineer cannot edit client details.");
            alert.showAndWait();
            return;
        }
        String name = txfClientName.getText().trim();
        String phone = txfPhoneNumber.getText().trim();
        String password = txfPassword.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Client name, phone, and password cannot be empty.");
            alert.showAndWait();
            return;
        }

        try {
            // Update database
            DBUtil.updateClient(clientId, name, phone, password);

            // Update Session
            Session.clientName = name;
            Session.phoneNumber = phone;
            Session.clientPassword = password;

            // Navigate back to modifiers dashboard
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/modifiers-dashboard-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Client Updated");
            alert.setContentText("Client information has been updated successfully.");
            alert.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Update Failed");
            alert.setContentText("Failed to update client: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancel(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/modifiers-dashboard-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
