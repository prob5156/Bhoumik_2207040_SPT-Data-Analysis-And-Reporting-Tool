package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AnalysisController {

    @FXML private TextArea analysisText;
    @FXML private TextArea finalComment;
    @FXML private Button btnAuthorize;
    @FXML private Label lblFinal;

    public void initialize() {
        analysisText.setText(
                "Depth 1.5m: Medium dense sandy soil\n" +
                        "Depth 3.0m: Dense sand layer\n" +
                        "Depth 4.5m: Stiff clay layer"
        );

        if(Session.role.equals("SUB")){
            finalComment.setDisable(true);
            btnAuthorize.setDisable(true);
            lblFinal.setText("Senior Engineer Final Comment (Read Only)");
        }
    }

    public void authorize(ActionEvent e){
        if(Session.role.equals("SENIOR")){
            btnAuthorize.setText("Authorized ✓");
            btnAuthorize.setDisable(true);
        }
    }

    public void back(ActionEvent e){
        go("dashboard-view.fxml", e);
    }

    private void go(String fxml, ActionEvent e){
        try{
            Scene sc = new Scene(
                    new FXMLLoader(
                            getClass().getResource("/com/example/sptdataanalysisandreportingtool/" + fxml)
                    ).load()
            );
            Stage st = (Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
            st.setScene(sc);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
