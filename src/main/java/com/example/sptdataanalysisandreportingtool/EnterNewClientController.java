package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class EnterNewClientController {

    @FXML private TextField tfLocationName;
    @FXML private TextField tfBoreHoles;
    @FXML private Label lblHeader;
    @FXML private Label lblClientName;

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
    }

    @FXML
    private void submit(ActionEvent e) throws java.io.IOException {
        String locationName = tfLocationName.getText();
        String boreHoles = tfBoreHoles.getText();

        if (locationName.isEmpty() || boreHoles.isEmpty()) {
            return;
        }

        try {
            int holes = Integer.parseInt(boreHoles);

            // persist location to DB for current client
            if (Session.clientId <= 0) {
                // no client selected
                return;
            }
            DBUtil.insertLocation(Session.clientId, locationName, holes);

            // Navigate back to locations panel
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/client-locations-panel-view.fxml")
            );
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
            s.centerOnScreen();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
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
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

