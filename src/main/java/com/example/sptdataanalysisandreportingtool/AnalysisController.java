package com.example.sptdataanalysisandreportingtool;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class AnalysisController {

    @FXML
    private Button btnAnalyze;

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblHeader;

    @FXML
    private TextArea txtResult;

    @FXML
    private void startAnalysis() {

        btnAnalyze.setDisable(true);
        lblStatus.setText("Processing SPT data...");

        Task<String> analysisTask = new Task<>() {

            @Override
            protected String call() throws Exception {

                Thread.sleep(3000);

                StringBuilder result = new StringBuilder();
                result.append("SPT Analysis Result\n");
                result.append("--------------------\n");
                result.append("Depth: 0 – 1.5 m → Loose Sand\n");
                result.append("Depth: 1.5 – 4.5 m → Medium Dense Sand\n");
                result.append("Depth: 4.5 – 6.0 m → Dense Sand\n");

                return result.toString();
            }
        };

        analysisTask.setOnSucceeded(e -> {
            txtResult.setText(analysisTask.getValue());
            lblStatus.setText("Analysis completed successfully.");
            btnAnalyze.setDisable(false);
        });

        analysisTask.setOnFailed(e -> {
            lblStatus.setText("Analysis failed.");
            btnAnalyze.setDisable(false);
        });

        Thread t = new Thread(analysisTask);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void initialize() {
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else if ("CLIENT".equalsIgnoreCase(Session.role)) lblHeader.setText("Client");
            else lblHeader.setText("");
        }
    }

    @FXML
    private void back(ActionEvent e) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/dashboard-view.fxml")
            );
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
