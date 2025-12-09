package com.example.sptdataanalysisandreportingtool;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RawDataController {

    public void goDashboard(ActionEvent e){
        goTo("dashboard-view.fxml", e);
    }

    public void goAnalysis(ActionEvent e){
        goTo("analysis-view.fxml", e);
    }

    public void addReading(ActionEvent e){
        // You will fill this later
    }

    private void goTo(String fxml, ActionEvent e){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene sc = new Scene(loader.load());
            Stage st = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
            st.setScene(sc);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
