package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.geometry.Insets;

public class BoreholeDashboardController {

    @FXML private Label lblHeader;
    @FXML private Label lblLocationName;
    @FXML private Label lblBoreHoles;
    @FXML private javafx.scene.control.Button btnEditLocation;
    @FXML private VBox vboxBoreholes;
    @FXML private javafx.scene.control.TextField tfSearchBoreholes;

    private int locationId;
    private String locationName;
    private int boreHoles;

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else lblHeader.setText("");
        }

        // Get location data from Session
        locationId = Session.editLocationId;
        locationName = Session.editLocationName;
        boreHoles = Session.editBoreHoles;

        if (lblLocationName != null) {
            lblLocationName.setText(locationName);
        }

        if (lblBoreHoles != null) {
            lblBoreHoles.setText(String.valueOf(boreHoles));
        }

        if (vboxBoreholes != null) {
            refreshBoreholes();
        }

        if (tfSearchBoreholes != null) {
            tfSearchBoreholes.textProperty().addListener((obs, oldVal, newVal) -> {
                filterBoreholes(newVal);
            });
        }

        // hide edit button for clients
        if (btnEditLocation != null) {
            btnEditLocation.setVisible(!"CLIENT".equalsIgnoreCase(Session.role));
        }
    }

    private void refreshBoreholes() {
        vboxBoreholes.getChildren().clear();

        // Create a sticker for each borehole
        for (int i = 1; i <= boreHoles; i++) {
            final int boreholeNum = i;
            Button boreholeBtn = new Button("Borehole " + i);
            
            String[] palettes = new String[] {
                "linear-gradient(to bottom right, #ff9a9e, #fecfef)",
                "linear-gradient(to bottom right, #a18cd1, #fbc2eb)",
                "linear-gradient(to bottom right, #f6d365, #fda085)",
                "linear-gradient(to bottom right, #84fab0, #8fd3f4)",
                "linear-gradient(to bottom right, #ffd3a5, #fd6585)"
            };
            int idx = Math.abs(("borehole-" + i).hashCode());
            String bg = palettes[idx % palettes.length];
            
            boreholeBtn.setStyle(
                "-fx-background-radius:10; -fx-background-insets:0; -fx-font-weight:bold; -fx-text-fill:white; -fx-background-color: " + bg + ";"
            );
            boreholeBtn.setPrefWidth(200);
            boreholeBtn.setPrefHeight(60);
            boreholeBtn.setWrapText(true);
            boreholeBtn.setPadding(new Insets(8));

            // Click to open rawdata-view
            boreholeBtn.setOnAction(ev -> {
                Session.selectedBorehole = boreholeNum;
                try {
                    FXMLLoader f = new FXMLLoader(
                            getClass().getResource("/com/example/sptdataanalysisandreportingtool/rawdata-view.fxml")
                    );
                    Stage s = (Stage) ((Node) ev.getSource()).getScene().getWindow();
                    Scene sc = new Scene(f.load());
                    sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                    s.setScene(sc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            vboxBoreholes.getChildren().add(boreholeBtn);
        }
    }

    private void filterBoreholes(String q) {
        if (vboxBoreholes == null) return;
        String query = q == null ? "" : q.trim().toLowerCase();
        for (javafx.scene.Node n : vboxBoreholes.getChildren()) {
            if (n instanceof javafx.scene.control.Button) {
                javafx.scene.control.Button btn = (javafx.scene.control.Button) n;
                String name = btn.getText() == null ? "" : btn.getText().toLowerCase();
                btn.setVisible(query.isEmpty() || name.contains(query));
                btn.setManaged(query.isEmpty() || name.contains(query));
            }
        }
    }

    @FXML
    private void editLocation(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/edit-location-view.fxml")
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
    private void back(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/client-locations-panel-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void refreshPanel() {
        // Reload data from Session (in case it was edited)
        boreHoles = Session.editBoreHoles;
        if (lblBoreHoles != null) {
            lblBoreHoles.setText(String.valueOf(boreHoles));
        }
        refreshBoreholes();
    }
}
