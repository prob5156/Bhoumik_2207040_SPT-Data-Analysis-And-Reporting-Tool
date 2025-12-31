package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController {

    @FXML
    private Label lblHeader;

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else if ("CLIENT".equalsIgnoreCase(Session.role)) lblHeader.setText("Client");
            else lblHeader.setText("");
        }
    }

    private void load(String fxml, ActionEvent e) {
        try {
            FXMLLoader f=new FXMLLoader(
                    getClass().getResource(fxml)
            );
            Stage s=(Stage)((Node)e.getSource()).getScene().getWindow();
            s.setScene(new Scene(f.load()));
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void openRaw(ActionEvent e){
        load("rawdata-view.fxml",e);
    }

    public void openAnalysis(ActionEvent e){
        load("analysis-view.fxml",e);
    }
}
