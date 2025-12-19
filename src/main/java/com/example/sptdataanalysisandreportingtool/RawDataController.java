package com.example.sptdataanalysisandreportingtool;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RawDataController {

    @FXML private TextField tfDepth;
    @FXML private TextField tfN1;
    @FXML private TextField tfN2;
    @FXML private TextField tfN3;
    @FXML private Button btnAdd;

    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row,String> colDepth;
    @FXML private TableColumn<Row,String> colN1;
    @FXML private TableColumn<Row,String> colN2;
    @FXML private TableColumn<Row,String> colN3;
    @FXML private TableColumn<Row,String> colNR;

    private final ObservableList<Row> data = FXCollections.observableArrayList();

    public void initialize() {
        colDepth.setCellValueFactory(d -> d.getValue().depth);
        colN1.setCellValueFactory(d -> d.getValue().n1);
        colN2.setCellValueFactory(d -> d.getValue().n2);
        colN3.setCellValueFactory(d -> d.getValue().n3);
        colNR.setCellValueFactory(d -> d.getValue().nr);
        table.setItems(data);

        if(Session.role.equals("SENIOR")){
            btnAdd.setDisable(true);
        }
    }

    public void addRow(ActionEvent e){
        String d=tfDepth.getText();
        String n1=tfN1.getText();
        String n2=tfN2.getText();
        String n3=tfN3.getText();

        if(d.isEmpty()||n1.isEmpty()||n2.isEmpty()||n3.isEmpty()) return;

        int nr=Integer.parseInt(n2)+Integer.parseInt(n3);
        data.add(new Row(d,n1,n2,n3,String.valueOf(nr)));

        tfDepth.clear();
        tfN1.clear();
        tfN2.clear();
        tfN3.clear();
    }

    public void back(ActionEvent e){
        go("dashboard-view.fxml",e);
    }

    public void analysis(ActionEvent e){
        go("analysis-view.fxml",e);
    }

    private void go(String fxml,ActionEvent e){
        try{
            Scene sc=new Scene(
                    new FXMLLoader(
                            getClass().getResource("/com/example/sptdataanalysisandreportingtool/"+fxml)
                    ).load()
            );
            Stage st=(Stage)((javafx.scene.Node)e.getSource()).getScene().getWindow();
            st.setScene(sc);
        }catch(Exception ex){ex.printStackTrace();}
    }

    public static class Row{
        javafx.beans.property.SimpleStringProperty depth;
        javafx.beans.property.SimpleStringProperty n1;
        javafx.beans.property.SimpleStringProperty n2;
        javafx.beans.property.SimpleStringProperty n3;
        javafx.beans.property.SimpleStringProperty nr;

        Row(String d,String a,String b,String c,String r){
            depth=new javafx.beans.property.SimpleStringProperty(d);
            n1=new javafx.beans.property.SimpleStringProperty(a);
            n2=new javafx.beans.property.SimpleStringProperty(b);
            n3=new javafx.beans.property.SimpleStringProperty(c);
            nr=new javafx.beans.property.SimpleStringProperty(r);
        }
    }
}
