package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.Node;

import java.sql.ResultSet;

public class RawDataController {

    @FXML private TextField tfDepth, tfSampleId, tfN1, tfN2, tfN3;
    @FXML private TextField tfColor, tfSand, tfSilt, tfClay;
    @FXML private TextField tfFromFeet, tfToFeet;
    @FXML private Label lblHeader;

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TableColumn<ObservableList<String>, String> colId, colDepth, colN1, colN2, colN3, colSum, colDescription;

    @FXML private VBox rootVBox;
    @FXML private javafx.scene.layout.HBox hboxEntry;
    @FXML private javafx.scene.layout.HBox hboxVisual;
    @FXML private javafx.scene.layout.HBox hboxDescribe;
    @FXML private javafx.scene.layout.HBox hboxActions;
    @FXML private Button btnAdd, btnDescribe, btnUpdate, btnDelete, btnDeleteVisualClassification;
    @FXML private Button btnBack;

    private int sel = -1;
    private int visualClassSelId = -1;

    @FXML
    public void initialize() {
        DBUtil.init();

        // set header based on role
        if (/*lblHeader placeholder check*/ true) {
            try {
                if (lblHeader != null) {
                    if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
                    else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
                    else if ("CLIENT".equalsIgnoreCase(Session.role)) lblHeader.setText("Client");
                    else lblHeader.setText("");
                }
            } catch (Exception ex) {
                // ignore header errors
            }
        }

        // Table row structure: [id, sample_code, depth, n1, n2, n3, sum, description]
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(1)));
        colDepth.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(2)));
        colN1.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(3)));
        colN2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(4)));
        colN3.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(5)));
        colSum.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(6)));
        colDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().get(7)));

        // For CLIENT role, make table view-only and expand it to fill available space
        if ("CLIENT".equalsIgnoreCase(Session.role)) {
            if (hboxEntry != null) hboxEntry.setVisible(false);
            if (hboxVisual != null) hboxVisual.setVisible(false);
            if (hboxDescribe != null) hboxDescribe.setVisible(false);
            if (hboxActions != null) hboxActions.setVisible(false);

            // Ensure only the table remains in the main VBox so it stretches
            if (rootVBox != null && table != null) {
                // keep the table and the Back button; remove other nodes
                rootVBox.getChildren().removeIf(n -> n != table && n != btnBack);
                VBox.setVgrow(table, Priority.ALWAYS);
                table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            }
            table.setEditable(false);
        } else {
            table.setEditable(true);
        }
        colId.setCellFactory(TextFieldTableCell.forTableColumn());
        colId.setOnEditCommit(e -> {
            try {
                ObservableList<String> row = e.getRowValue();
                String oldVal = e.getOldValue();
                String newVal = e.getNewValue();
                row.set(1, newVal);
                int id = Integer.parseInt(row.get(0));
                DBUtil.updateSptSampleCode(id, newVal);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        table.setOnMouseClicked(e -> {
            pick();
            pickVisualClassification();
        });
        load();
    }

    private void load() {
        table.getItems().clear();
        try {
            ResultSet r = DBUtil.fetchSptDataByBorehole(Session.selectedBorehole, Session.editLocationId);
            while (r.next()) {
                ObservableList<String> o = FXCollections.observableArrayList();
                int id = r.getInt("id");
                o.add(String.valueOf(id));
                o.add(r.getString("sample_code"));
                o.add(r.getString("depth"));
                o.add(r.getString("n1"));
                o.add(r.getString("n2"));
                o.add(r.getString("n3"));
                int n2 = r.getInt("n2");
                int n3 = r.getInt("n3");
                o.add(String.valueOf(n2 + n3));
                o.add("");
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
        tfSampleId.setText(r.get(1));
        tfDepth.setText(r.get(2));
        tfN1.setText(r.get(3));
        tfN2.setText(r.get(4));
        tfN3.setText(r.get(5));
    }

    private void pickVisualClassification() {
        try {
            ObservableList<String> r = table.getSelectionModel().getSelectedItem();
            if (r == null) return;

            double depth = Double.parseDouble(r.get(2));
            VisualClassification vc = DBUtil.fetchVisualClassificationForDepth(Session.selectedBorehole, Session.editLocationId, depth);

            if (vc != null) {
                visualClassSelId = vc.id;
                tfColor.setText(vc.colorCode);
                tfSand.setText(String.valueOf(vc.sandPercentage));
                tfSilt.setText(String.valueOf(vc.siltPercentage));
                tfClay.setText(String.valueOf(vc.clayPercentage));
                tfFromFeet.setText(String.valueOf(vc.fromDepth));
                tfToFeet.setText(String.valueOf(vc.toDepth));
            } else {
                // No visual classification for this depth - clear fields
                visualClassSelId = -1;
                tfColor.clear();
                tfSand.clear();
                tfSilt.clear();
                tfClay.clear();
                tfFromFeet.clear();
                tfToFeet.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add() {
        try {
            double newDepth = Double.parseDouble(tfDepth.getText());
            
            // Validate depth is in decreasing order
            if (!table.getItems().isEmpty()) {
                ObservableList<String> lastRow = table.getItems().get(table.getItems().size() - 1);
                double lastDepth = Double.parseDouble(lastRow.get(2));
                if (newDepth >= lastDepth) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Depth");
                    alert.setHeaderText("Depth Order Violation");
                    alert.setContentText("New depth must be less than the previous depth (" + lastDepth + "). Current: " + newDepth);
                    alert.showAndWait();
                    return;
                }
            }
            
            DBUtil.insertSptData(
                    Session.selectedBorehole,
                    Session.editLocationId,
                    tfSampleId.getText(),
                    newDepth,
                    Integer.parseInt(tfN1.getText()),
                    Integer.parseInt(tfN2.getText()),
                    Integer.parseInt(tfN3.getText())
            );
            load();
            tfSampleId.clear();
            tfDepth.clear();
            tfN1.clear();
            tfN2.clear();
            tfN3.clear();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Number Format Error");
            alert.setContentText("Please enter valid numbers for all fields.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        if (sel == -1) return;
        try {
            DBUtil.updateSptData(
                    sel,
                    tfSampleId.getText(),
                    Double.parseDouble(tfDepth.getText()),
                    Integer.parseInt(tfN1.getText()),
                    Integer.parseInt(tfN2.getText()),
                    Integer.parseInt(tfN3.getText())
            );
            load();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        if (sel == -1) return;
        try {
            DBUtil.deleteSptData(sel);
            load();
            sel = -1;
            tfSampleId.clear();
            tfDepth.clear();
            tfN1.clear();
            tfN2.clear();
            tfN3.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteVisualClassification() {
        if (visualClassSelId == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Visual Classification Selected");
            alert.setContentText("Please select a row with a visual classification to delete.");
            alert.showAndWait();
            return;
        }
        try {
            int idToDelete = visualClassSelId;
            DBUtil.deleteVisualClassification(idToDelete);
            visualClassSelId = -1;
            tfColor.clear();
            tfSand.clear();
            tfSilt.clear();
            tfClay.clear();
            tfFromFeet.clear();
            tfToFeet.clear();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Deleted");
            alert.setHeaderText("Visual Classification Deleted");
            alert.setContentText("Visual classification has been deleted successfully.");
            alert.showAndWait();
            
            load();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Delete Failed");
            alert.setContentText("Failed to delete visual classification: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void back(ActionEvent e) {
        try {
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            FXMLLoader f = new FXMLLoader(
                    getClass().getResource("/com/example/sptdataanalysisandreportingtool/borehole-dashboard-view.fxml")
            );
            s.setScene(new Scene(f.load()));
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void describe() {
        try {
            String colorCode = tfColor.getText();
            double sand = Double.parseDouble(tfSand.getText());
            double silt = Double.parseDouble(tfSilt.getText());
            double clay = Double.parseDouble(tfClay.getText());
            double fromDepth = Double.parseDouble(tfFromFeet.getText());
            double toDepth = Double.parseDouble(tfToFeet.getText());

            // Validate percentages sum to 100
            double total = sand + silt + clay;
            if (Math.abs(total - 100.0) > 0.01) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Percentages");
                alert.setHeaderText("Percentage Sum Validation");
                alert.setContentText("Sand% + Silt% + Clay% must equal 100.\nCurrent sum: " + String.format("%.2f", total));
                alert.showAndWait();
                return;
            }

            // Validate depth range - To should be greater than From
            if (fromDepth >= toDepth) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Depth Range");
                alert.setHeaderText("Depth Order Error");
                alert.setContentText("To(feet) must be greater than From(feet).");
                alert.showAndWait();
                return;
            }

            if (visualClassSelId == -1) {
                // Insert new visual classification record
                DBUtil.insertVisualClassification(
                        Session.selectedBorehole,
                        Session.editLocationId,
                        colorCode,
                        sand,
                        silt,
                        clay,
                        fromDepth,
                        toDepth
                );
            } else {
                // Update existing visual classification record
                DBUtil.updateVisualClassification(
                        visualClassSelId,
                        colorCode,
                        sand,
                        silt,
                        clay,
                        fromDepth,
                        toDepth
                );
            }

            // Clear fields after saving
            tfColor.clear();
            tfSand.clear();
            tfSilt.clear();
            tfClay.clear();
            tfFromFeet.clear();
            tfToFeet.clear();
            visualClassSelId = -1;

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Visual Classification Saved");
            alert.setContentText("Visual classification data has been saved successfully.");
            alert.showAndWait();
            
            load();
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please enter valid numbers for all percentage and depth fields.");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Save Failed");
            alert.setContentText("Failed to save visual classification: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
