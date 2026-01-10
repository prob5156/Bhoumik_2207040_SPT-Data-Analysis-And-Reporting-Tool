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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @FXML private Button btnAnalysis;
    @FXML private Button btnAnalysisBottom;

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
            if (btnAnalysisBottom != null) btnAnalysisBottom.setVisible(true);
        } else {
            table.setEditable(true);
            if (btnAnalysisBottom != null) btnAnalysisBottom.setVisible(false);
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
        String sql = "SELECT * FROM spt_data WHERE borehole_id=? AND location_id=? ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, Session.selectedBorehole);
            p.setInt(2, Session.editLocationId);
            try (ResultSet r = p.executeQuery()) {
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
            }

            // After loading rows, apply visual classification descriptions
            applyVisualClassificationDescriptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyVisualClassificationDescriptions() {
        String sql = "SELECT * FROM visual_classification WHERE borehole_id=? AND location_id=? ORDER BY from_depth";
        // Clear existing descriptions first
        for (ObservableList<String> row : table.getItems()) {
            if (row.size() > 7) row.set(7, "");
        }

        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, Session.selectedBorehole);
            p.setInt(2, Session.editLocationId);
            try (ResultSet rs = p.executeQuery()) {
                while (rs.next()) {
                    double from = rs.getDouble("from_depth");
                    double to = rs.getDouble("to_depth");
                    String color = rs.getString("color_code");
                    double sand = rs.getDouble("sand_percentage");
                    double silt = rs.getDouble("silt_percentage");
                    double clay = rs.getDouble("clay_percentage");

                    // collect total SPT values for rows within range
                    double minTotal = Double.POSITIVE_INFINITY;
                    double maxTotal = Double.NEGATIVE_INFINITY;
                    for (ObservableList<String> row : table.getItems()) {
                        try {
                            double depth = Double.parseDouble(row.get(2));
                            if (depth >= from && depth <= to) {
                                String sumStr = row.get(6);
                                if (sumStr == null || sumStr.isEmpty()) continue;
                                double val = Double.parseDouble(sumStr);
                                if (val < minTotal) minTotal = val;
                                if (val > maxTotal) maxTotal = val;
                            }
                        } catch (Exception ex) {
                            // ignore parse errors
                        }
                    }

                    if (minTotal == Double.POSITIVE_INFINITY) {
                        // no spt data in range - skip
                        continue;
                    }

                    // Decide which SPT condition table to use
                    boolean useTable2 = sand > clay;

                    String condLow = mapSptToCondition(minTotal, useTable2);
                    String condHigh = mapSptToCondition(maxTotal, useTable2);
                    String conditionPart = condLow.equals(condHigh) ? condLow : condLow + " / " + condHigh;

                    // Map percentages to terms
                    java.util.Map<String, Double> percMap = new java.util.HashMap<>();
                    percMap.put("sand", sand);
                    percMap.put("silt", silt);
                    percMap.put("clay", clay);

                    // For each percentage get term from Table-1
                    java.util.Map<String, String> termMap = new java.util.HashMap<>();
                    for (java.util.Map.Entry<String, Double> e : percMap.entrySet()) {
                        double pVal = e.getValue();
                        if (pVal <= 0) continue;
                        termMap.put(e.getKey(), mapPercentageToTerm(pVal));
                    }

                    // Order components by percentage desc
                    java.util.List<java.util.Map.Entry<String, Double>> comps = new java.util.ArrayList<>(percMap.entrySet());
                    comps.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

                    // Build component string: highest -> NAME (uppercase), others -> "term name"; external rule: if sand lowest call it "fine sand"
                    StringBuilder compSb = new StringBuilder();
                    for (int i = 0; i < comps.size(); i++) {
                        String name = comps.get(i).getKey();
                        double pct = comps.get(i).getValue();
                        if (pct <= 0) continue;
                        if (i == 0) {
                            compSb.append(name.toUpperCase());
                        } else {
                            String term = termMap.getOrDefault(name, "");
                            String displayName = name;
                            // external rule: if sand is lowest then display 'fine sand'
                            if (name.equals("sand")) {
                                double sandPct = percMap.getOrDefault("sand", 0.0);
                                double siltPct = percMap.getOrDefault("silt", 0.0);
                                double clayPct = percMap.getOrDefault("clay", 0.0);
                                if (sandPct <= siltPct && sandPct <= clayPct) {
                                    displayName = "fine sand";
                                }
                            }
                            if (!term.isEmpty()) {
                                if (compSb.length() > 0) compSb.append(", ");
                                compSb.append(term.toLowerCase()).append(" ").append(displayName);
                            } else {
                                if (compSb.length() > 0) compSb.append(", ");
                                compSb.append(displayName);
                            }
                        }
                    }

                    // Compose final description: full color name + condition + components
                    String colorFull = mapColorCode(color);
                    String desc = colorFull;
                    if (!conditionPart.isEmpty()) desc += " " + conditionPart.toLowerCase();
                    if (compSb.length() > 0) desc += " " + compSb.toString();

                    // write description only on first matching row to simulate merged cell
                    boolean firstSet = false;
                    for (ObservableList<String> row : table.getItems()) {
                        try {
                            double depth = Double.parseDouble(row.get(2));
                            if (depth >= from && depth <= to) {
                                if (!firstSet) {
                                    row.set(7, desc);
                                    firstSet = true;
                                } else {
                                    row.set(7, "");
                                }
                            }
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String mapPercentageToTerm(double p) {
        if (p < 5) return "Trace";
        if (p < 10) return "Few";
        if (p < 25) return "Little";
        if (p < 45) return "Some";
        return "Mostly";
    }

    private String mapSptToCondition(double val, boolean useTable2) {
        if (useTable2) {
            // Table-2
            if (val <= 4) return "Very Loose";
            if (val <= 10) return "Loose";
            if (val <= 25) return "Medium Dense";
            if (val <= 50) return "Dense";
            return "Very Dense";
        } else {
            // Table-3
            if (val <= 2) return "Very Soft";
            if (val <= 4) return "Soft";
            if (val <= 8) return "Medium Stiff";
            if (val <= 16) return "Stiff";
            if (val <= 30) return "Very Stiff";
            return "Hard";
        }
    }

    private String mapColorCode(String code) {
        if (code == null) return "";
        switch (code.trim().toUpperCase()) {
            case "RB": return "Redish Brown";
            case "BL": return "Black";
            case "BR": return "Brown";
            default: return code;
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
            
            // Validate depth is in increasing order (new depth must be greater than last)
            if (!table.getItems().isEmpty()) {
                ObservableList<String> lastRow = table.getItems().get(table.getItems().size() - 1);
                double lastDepth = Double.parseDouble(lastRow.get(2));
                if (newDepth <= lastDepth) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Depth");
                    alert.setHeaderText("Depth Order Violation");
                    alert.setContentText("New depth must be greater than the previous depth (" + lastDepth + "). Current: " + newDepth);
                    alert.showAndWait();
                    return;
                }
            }
            
                String sampleCode = tfSampleId.getText();
                if (sampleCode == null || sampleCode.trim().isEmpty()) {
                sampleCode = computeNextSampleCode(Session.selectedBorehole, Session.editLocationId);
                }

                DBUtil.insertSptData(
                    Session.selectedBorehole,
                    Session.editLocationId,
                    sampleCode,
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

    /**
     * Compute next sample code based on existing sample_code values for the borehole+location.
     * Accepts patterns like LETTERS + NUMBER + optional LETTERS (e.g. D1, D1A, S10B).
     * Next code will use the same prefix letters if found; numeric part increments to max+1.
     */
    private String computeNextSampleCode(int boreholeId, int locationId) {
        String prefix = "D"; // default prefix
        int maxNum = 0;
        Pattern p = Pattern.compile("^([A-Za-z]+)(\\d+)([A-Za-z]*)$");
        String sql = "SELECT sample_code FROM spt_data WHERE borehole_id=? AND location_id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, boreholeId);
            ps.setInt(2, locationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String sc = rs.getString("sample_code");
                    if (sc == null || sc.trim().isEmpty()) continue;
                    Matcher m = p.matcher(sc.trim());
                    if (m.matches()) {
                        String pf = m.group(1);
                        String numS = m.group(2);
                        int num = Integer.parseInt(numS);
                        if (pf != null && !pf.isEmpty()) prefix = pf; // keep latest prefix found
                        if (num > maxNum) maxNum = num;
                    } else {
                        // if no match but has leading letters and digits, try to extract digits
                        String digits = sc.replaceAll("[^0-9]", "");
                        if (!digits.isEmpty()) {
                            int num = Integer.parseInt(digits);
                            if (num > maxNum) maxNum = num;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        int next = maxNum + 1;
        return prefix + next;
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
                Scene sc = new Scene(f.load());
                sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                s.setScene(sc);
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

    @FXML
    public void analysis(ActionEvent e) {
        if (sel == -1) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Row Selected");
            alert.setContentText("Please select a row to analyze.");
            alert.showAndWait();
            return;
        }

        try {
            Session.selectedSptId = sel;
            FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/analysis-view.fxml"));
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            Scene sc = new Scene(f.load());
            sc.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            s.setScene(sc);
            s.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    
}
