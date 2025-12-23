package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class DashboardController {

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
