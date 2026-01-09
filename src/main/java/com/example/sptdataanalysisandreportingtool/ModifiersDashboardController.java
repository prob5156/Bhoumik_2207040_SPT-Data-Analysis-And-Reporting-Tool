package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.util.Map;

public class ModifiersDashboardController {

    @FXML
    private Label lblHeader;

    @FXML
    private FlowPane flowPaneClients;

    @FXML
    private javafx.scene.control.Button btnEnterNewClient;

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else lblHeader.setText("");
        }

        if (flowPaneClients != null) {
            refreshClients();
        }

        // Hide the 'Enter New Client' button for SUB users
        if (btnEnterNewClient != null) {
            if ("SUB".equalsIgnoreCase(Session.role)) btnEnterNewClient.setVisible(false);
            else btnEnterNewClient.setVisible(true);
        }
    }

    private void load(String fxml, ActionEvent e) {
        try {
            FXMLLoader f=new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/" + fxml)
            );
            Stage s=(Stage)((Node)e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void seeOldClients(ActionEvent e){
        load("rawdata-view.fxml",e);
    }

    public void enterNewClients(ActionEvent e){
        // Prevent SUB role from entering new clients
        if ("SUB".equalsIgnoreCase(Session.role)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Insufficient Permissions");
            alert.setContentText("Subconductor Engineer cannot create new clients.");
            alert.showAndWait();
            return;
        }

        load("client-details-view.fxml",e);
    }

    private void refreshClients() {
        flowPaneClients.getChildren().clear();

        try {
            var rs = DBUtil.fetchClients();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String phone = rs.getString("phone");

                // Create a VBox to hold the button and edit button
                VBox clientBox = new VBox(8);
                clientBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 8;");

                // Main client button
                Button b = new Button(name);
                String[] palettes = new String[] {
                        "linear-gradient(to bottom right, #ff9a9e, #fecfef)",
                        "linear-gradient(to bottom right, #a18cd1, #fbc2eb)",
                        "linear-gradient(to bottom right, #f6d365, #fda085)",
                        "linear-gradient(to bottom right, #84fab0, #8fd3f4)",
                        "linear-gradient(to bottom right, #ffd3a5, #fd6585)"
                };
                int i = Math.abs((name + "::clients").hashCode());
                String bg = palettes[i % palettes.length];
                b.setStyle("-fx-background-radius:10; -fx-background-insets:0; -fx-font-weight:bold; -fx-text-fill:white; -fx-background-color: " + bg + ";");
                b.setPrefWidth(180);
                b.setPrefHeight(70);
                b.setWrapText(true);
                b.setPadding(new Insets(8));

                b.setOnAction(ev -> {
                    Session.clientName = name;
                    Session.phoneNumber = phone;
                    Session.clientId = id;
                    try {
                        FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/client-locations-panel-view.fxml"));
                        Stage s = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                        s.setScene(new Scene(f.load()));
                        s.centerOnScreen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                // Only SENIOR role can edit/delete clients
                if ("SENIOR".equalsIgnoreCase(Session.role)) {
                    // Edit button
                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
                    editBtn.setPrefWidth(90);

                    editBtn.setOnAction(ev -> {
                        Session.clientId = id;
                        Session.clientName = name;
                        Session.phoneNumber = phone;
                        try {
                            FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/edit-client-view.fxml"));
                            Stage s = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                            s.setScene(new Scene(f.load()));
                            s.centerOnScreen();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    // Delete button
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
                    deleteBtn.setPrefWidth(90);

                    deleteBtn.setOnAction(ev -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Delete");
                        alert.setHeaderText("Delete Client");
                        alert.setContentText("Are you sure you want to delete this client? This action cannot be undone.");
                        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                            try {
                                DBUtil.deleteClient(id);
                                refreshClients();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Error");
                                errorAlert.setHeaderText("Delete Failed");
                                errorAlert.setContentText("Failed to delete client: " + ex.getMessage());
                                errorAlert.showAndWait();
                            }
                        }
                    });

                    // HBox to hold Edit and Delete buttons side by side
                    javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(8);
                    buttonBox.getChildren().addAll(editBtn, deleteBtn);

                    clientBox.getChildren().addAll(b, buttonBox);
                } else {
                    // Non-senior roles only see client button
                    clientBox.getChildren().add(b);
                }

                flowPaneClients.getChildren().add(clientBox);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
