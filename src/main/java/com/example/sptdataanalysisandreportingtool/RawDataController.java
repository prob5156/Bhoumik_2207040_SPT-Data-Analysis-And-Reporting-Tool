package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;

import java.sql.ResultSet;

public class RawDataController {

    @FXML private TextField tfDepth, tfN1, tfN2, tfN3;
    @FXML private TextArea taComment;

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TableColumn<ObservableList<String>, String> colId, colDepth, colN1, colN2, colN3, colComment;

    private int sel = -1;

    @FXML
    public void initialize() {
        DBUtil.init();

        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(0)));
        colDepth.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colN1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colN2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colN3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colComment.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));

        table.setOnMouseClicked(e -> pick());
        load();
    }

    private void load() {
        table.getItems().clear();
        try {
            ResultSet r = DBUtil.fetchAll();
            while (r.next()) {
                ObservableList<String> o = FXCollections.observableArrayList();
                o.add(r.getString("id"));
                o.add(r.getString("depth"));
                o.add(r.getString("n1"));
                o.add(r.getString("n2"));
                o.add(r.getString("n3"));
                o.add(r.getString("comment"));
                table.getItems().add(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pick() {
        ObservableList<String> r = table.getSelectionModel().getSelectedItem();
        if (r == null) return;

        sel = Integer.parseInt(r.get(0));
        tfDepth.setText(r.get(1));
        tfN1.setText(r.get(2));
        tfN2.setText(r.get(3));
        tfN3.setText(r.get(4));
        taComment.setText(r.get(5));
    }

    public void add() {
        try {
            DBUtil.insert(
                    Double.parseDouble(tfDepth.getText()),
                    Integer.parseInt(tfN1.getText()),
                    Integer.parseInt(tfN2.getText()),
                    Integer.parseInt(tfN3.getText()),
                    taComment.getText()
            );
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (sel == -1) return;
        try {
            DBUtil.update(
                    sel,
                    Double.parseDouble(tfDepth.getText()),
                    Integer.parseInt(tfN1.getText()),
                    Integer.parseInt(tfN2.getText()),
                    Integer.parseInt(tfN3.getText()),
                    taComment.getText()
            );
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        if (sel == -1) return;
        try {
            DBUtil.delete(sel);
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void back(ActionEvent e) {
        try {
            Stage s = (Stage) tfDepth.getScene().getWindow();
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("dashboard-view.fxml")
            );
            s.setScene(new Scene(f.load()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
