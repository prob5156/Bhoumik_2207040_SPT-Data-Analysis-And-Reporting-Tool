package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.geometry.Insets;

import java.util.Map;

public class ClientLocationsPanelController {

    @FXML private Label lblHeader;
    @FXML private Label lblClientName;
    @FXML private Label lblPhoneNumber;
    @FXML private FlowPane flowPaneLocations;
    @FXML private VBox actionsBox;

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else lblHeader.setText("");
        }

        if (lblClientName != null) {
            lblClientName.setText(Session.clientName);
        }

        if (lblPhoneNumber != null) {
            lblPhoneNumber.setText(Session.phoneNumber);
        }

        if (flowPaneLocations != null) {
            refreshLocations();
        }

        // hide action controls for clients
        if (actionsBox != null) {
            if ("CLIENT".equalsIgnoreCase(Session.role)) {
                actionsBox.setVisible(false);
            } else {
                actionsBox.setVisible(true);
            }
        }
    }

    private void refreshLocations() {
        flowPaneLocations.getChildren().clear();
        try {
            var rs = DBUtil.fetchLocationsByClient(Session.clientId);
            while (rs.next()) {
                int locId = rs.getInt("id");
                String locationName = rs.getString("location_name");
                int holes = rs.getInt("bore_holes");

                // Create a VBox to hold the location button and edit button
                VBox locationBox = new VBox(8);
                locationBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-padding: 8;");

                // Main location button - clickable to open borehole dashboard
                Button locationButton = new Button(locationName);
                // colorful sticker styles (cycle through a small palette)
                String[] palettes = new String[] {
                    "linear-gradient(to bottom right, #ff9a9e, #fecfef)",
                    "linear-gradient(to bottom right, #a18cd1, #fbc2eb)",
                    "linear-gradient(to bottom right, #f6d365, #fda085)",
                    "linear-gradient(to bottom right, #84fab0, #8fd3f4)",
                    "linear-gradient(to bottom right, #ffd3a5, #fd6585)"
                };
                int i = Math.abs((locationName + "::locations").hashCode());
                String bg = palettes[i % palettes.length];
                locationButton.setStyle(
                    "-fx-background-radius:10; -fx-background-insets:0; -fx-font-weight:bold; -fx-text-fill:white; -fx-background-color: " + bg + ";"
                );
                locationButton.setPrefWidth(160);
                locationButton.setPrefHeight(60);
                locationButton.setWrapText(true);
                locationButton.setPadding(new Insets(8));

                // Click location button to open borehole dashboard
                locationButton.setOnAction(ev -> {
                    Session.editLocationId = locId;
                    Session.editLocationName = locationName;
                    Session.editBoreHoles = holes;
                    try {
                        FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/borehole-dashboard-view.fxml"));
                        Stage s = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                        s.setScene(new Scene(f.load()));
                        s.centerOnScreen();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                locationButton.setWrapText(true);
                locationButton.setPadding(new Insets(8));

                if (!"CLIENT".equalsIgnoreCase(Session.role)) {
                    // Edit button
                    Button editBtn = new Button("Edit");
                    editBtn.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
                    editBtn.setPrefWidth(75);

                    editBtn.setOnAction(ev -> {
                        Session.editLocationId = locId;
                        Session.editLocationName = locationName;
                        Session.editBoreHoles = holes;
                        try {
                            FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/edit-location-view.fxml"));
                            Stage s = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                            s.setScene(new Scene(f.load()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                    // Delete button
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5;");
                    deleteBtn.setPrefWidth(75);

                    deleteBtn.setOnAction(ev -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Delete");
                        alert.setHeaderText("Delete Location");
                        alert.setContentText("Are you sure you want to delete this location? This action cannot be undone.");
                        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                            try {
                                DBUtil.deleteLocation(locId);
                                refreshLocations();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                errorAlert.setTitle("Error");
                                errorAlert.setHeaderText("Delete Failed");
                                errorAlert.setContentText("Failed to delete location: " + ex.getMessage());
                                errorAlert.showAndWait();
                            }
                        }
                    });

                    // HBox to hold Edit and Delete buttons side by side
                    javafx.scene.layout.HBox buttonBox = new javafx.scene.layout.HBox(8);
                    buttonBox.getChildren().addAll(editBtn, deleteBtn);

                    locationBox.getChildren().addAll(locationButton, buttonBox);
                    flowPaneLocations.getChildren().add(locationBox);
                } else {
                    // client role: only show the location button (no edit/delete)
                    locationBox.getChildren().add(locationButton);
                    flowPaneLocations.getChildren().add(locationBox);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void enterNewLocation(ActionEvent e) {
        try {
                FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/enter-new-client-view.fxml")
                );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
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
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshPanel() {
        refreshLocations();
    }
}
