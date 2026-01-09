package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class EditLocationController {

    @FXML private TextField txfLocationName;
    @FXML private TextField txfBoreHoles;

    private int locationId;
    private String locationName;
    private int boreHoles;

    @FXML
    public void initialize() {
        // Data should be set from ModifiersDashboardController before loading this view
        if (Session.editLocationId > 0) {
            locationId = Session.editLocationId;
            locationName = Session.editLocationName;
            boreHoles = Session.editBoreHoles;

            txfLocationName.setText(locationName);
            txfBoreHoles.setText(String.valueOf(boreHoles));
        }
    }

    @FXML
    private void save(ActionEvent e) {
        // Prevent SUB role from saving location edits
        if ("SUB".equalsIgnoreCase(Session.role)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Access Denied");
            alert.setHeaderText("Insufficient Permissions");
            alert.setContentText("Subconductor Engineer cannot edit locations.");
            alert.showAndWait();
            return;
        }
        String name = txfLocationName.getText().trim();
        String holesStr = txfBoreHoles.getText().trim();

        if (name.isEmpty() || holesStr.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Location name and bore holes cannot be empty.");
            alert.showAndWait();
            return;
        }

        try {
            int holes = Integer.parseInt(holesStr);
            if (holes < 0) {
                throw new NumberFormatException("Bore holes must be non-negative");
            }

            // Update database
            DBUtil.updateLocation(locationId, name, holes);

            // Update Session with new values
            Session.editLocationName = name;
            Session.editBoreHoles = holes;

            // Navigate back to borehole-dashboard (if coming from there) or client-locations-panel
            String targetView = "borehole-dashboard-view.fxml";
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/" + targetView)
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Location Updated");
            alert.setContentText("Location information has been updated successfully.");
            alert.showAndWait();
        } catch (NumberFormatException ex) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Bore holes must be a valid number.");
            alert.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Update Failed");
            alert.setContentText("Failed to update location: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void cancel(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/client-locations-panel-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
