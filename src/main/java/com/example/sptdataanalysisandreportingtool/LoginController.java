package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField tfUser;
    @FXML private PasswordField tfPass;

    @FXML
    private void login(ActionEvent e) {
        String phone = tfUser.getText().trim();
        String password = tfPass.getText().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Empty Fields");
            alert.setContentText("Please enter phone number and password.");
            alert.showAndWait();
            return;
        }

        try {
            // Authenticate client from database
            var rs = DBUtil.fetchClients();
            boolean found = false;

            while (rs.next()) {
                String dbPhone = rs.getString("phone");
                String dbPassword = rs.getString("password");
                String dbName = rs.getString("name");
                int dbId = rs.getInt("id");

                if (dbPhone.equals(phone) && dbPassword.equals(password)) {
                    // Authentication successful
                    Session.role = "CLIENT";
                    Session.clientId = dbId;
                    Session.clientName = dbName;
                    Session.phoneNumber = phone;
                    found = true;
                    break;
                }
            }

            if (!found) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Authentication Failed");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Phone number or password is incorrect.");
                alert.showAndWait();
                return;
            }

                // Navigate to client locations panel for authenticated client
                FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/client-locations-panel-view.fxml")
                );
                Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
                Scene sc = new Scene(f.load());
                sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Error");
            alert.setContentText("An error occurred during login: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void loginAsSenior(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("senior-login-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void loginAsSub(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("sub-login-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadDashboard(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("dashboard-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void openRegister(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Register");
        alert.setHeaderText(null);
        alert.setContentText("Registration is not available in this build.");
        alert.showAndWait();
    }
}
