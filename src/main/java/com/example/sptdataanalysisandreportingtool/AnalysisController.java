package com.example.sptdataanalysisandreportingtool;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AnalysisController {

    @FXML
    private Label lblStatus;

    @FXML
    private Label lblHeader;

    @FXML
    private TextArea txtResult;

    @FXML
    private TableView<PercentTermRow> tblPercentTerms;
    @FXML
    private TableColumn<PercentTermRow, String> colPercRange;
    @FXML
    private TableColumn<PercentTermRow, String> colTerm;

    @FXML
    private TableView<TotalSptRow> tblTable2;
    @FXML
    private TableColumn<TotalSptRow, String> colTotalRange2;
    @FXML
    private TableColumn<TotalSptRow, String> colCondition2;

    @FXML
    private TableView<TotalSptRow> tblTable3;
    @FXML
    private TableColumn<TotalSptRow, String> colTotalRange3;
    @FXML
    private TableColumn<TotalSptRow, String> colCondition3;

    // Analysis is generated automatically on initialize when an SPT is selected.

    @FXML
    public void initialize() {
        setupTables();
        if (lblHeader != null) {
            if ("SENIOR".equalsIgnoreCase(Session.role)) lblHeader.setText("Senior Executive Engineer");
            else if ("SUB".equalsIgnoreCase(Session.role)) lblHeader.setText("Subconductor Engineer");
            else if ("CLIENT".equalsIgnoreCase(Session.role)) lblHeader.setText("Client");
            else lblHeader.setText("");
        }

        // If a selected SPT id exists, generate the detailed analysis
        if (Session.selectedSptId > 0) {
            try {
                StringBuilder out = new StringBuilder();

                // Show reference tables
                out.append("Table-1 (Percent -> Term)\n");
                out.append("<5%: Trace, 5-10%: Few, 10-25%: Little, 25-45%: Some, 45-100%: Mostly\n\n");

                out.append("Table-2 (Total SPT -> Condition when Sand>Clay)\n");
                out.append("0-4: Very Loose, 4-10: Loose, 10-25: Medium Dense, 25-50: Dense, >50: Very Dense\n\n");

                out.append("Table-3 (Total SPT -> Condition when Sand<Clay)\n");
                out.append("0-2: Very Soft, 2-4: Soft, 4-8: Medium Stiff, 8-16: Stiff, 16-30: Very Stiff, >30: Hard\n\n");

                ResultSet sptRs = DBUtil.fetchSptDataById(Session.selectedSptId);
                if (!sptRs.next()) {
                    out.append("Selected SPT record not found.\n");
                    txtResult.setText(out.toString());
                    return;
                }

                double depth = sptRs.getDouble("depth");
                int boreholeId = sptRs.getInt("borehole_id");
                int locationId = sptRs.getInt("location_id");

                out.append(String.format("Selected SPT id=%d depth=%.2f (borehole=%d, location=%d)\n\n", Session.selectedSptId, depth, boreholeId, locationId));

                // Find visual classification covering this depth
                VisualClassification vc = DBUtil.fetchVisualClassificationForDepth(boreholeId, locationId, depth);
                if (vc == null) {
                    out.append("No visual classification record covers this depth.\n");
                    txtResult.setText(out.toString());
                    return;
                }

                out.append("Step 1: Color mapping\n");
                out.append(String.format("Code: %s -> %s\n\n", vc.colorCode, mapColorCode(vc.colorCode)));

                out.append("Step 2: Choose SPT table (compare Sand% and Clay%)\n");
                out.append(String.format("Sand=%.2f, Clay=%.2f -> %s\n\n", vc.sandPercentage, vc.clayPercentage,
                        vc.sandPercentage > vc.clayPercentage ? "Use Table-2" : "Use Table-3"));

                out.append(String.format("Step 3: Gather TOTAL SPT values for depths %.2f to %.2f\n", vc.fromDepth, vc.toDepth));

                // collect totals
                List<Double> totals = new ArrayList<>();
                ResultSet rows = DBUtil.fetchSptDataByBorehole(boreholeId, locationId);
                while (rows.next()) {
                    double d = rows.getDouble("depth");
                    if (d >= vc.fromDepth && d <= vc.toDepth) {
                        int n2 = rows.getInt("n2");
                        int n3 = rows.getInt("n3");
                        totals.add((double) (n2 + n3));
                        out.append(String.format("  depth %.2f -> total SPT = %d\n", d, n2 + n3));
                    }
                }
                if (totals.isEmpty()) {
                    out.append("No SPT rows found in the range.\n");
                    txtResult.setText(out.toString());
                    return;
                }

                double min = totals.stream().min(Comparator.naturalOrder()).orElse(0.0);
                double max = totals.stream().max(Comparator.naturalOrder()).orElse(0.0);
                out.append(String.format("\nMin total = %.0f, Max total = %.0f\n\n", min, max));

                out.append("Step 3 (continued): Map min/max to condition(s)\n");
                boolean useTable2 = vc.sandPercentage > vc.clayPercentage;
                String condLow = mapSptToCondition(min, useTable2);
                String condHigh = mapSptToCondition(max, useTable2);
                if (condLow.equals(condHigh)) out.append(String.format("Condition: %s\n\n", condLow));
                else out.append(String.format("Conditions: %s / %s\n\n", condLow, condHigh));

                out.append("Step 4: Map percentages to terms (Table-1)\n");
                Map<String, Double> perc = new HashMap<>();
                perc.put("sand", vc.sandPercentage);
                perc.put("silt", vc.siltPercentage);
                perc.put("clay", vc.clayPercentage);
                for (Map.Entry<String, Double> e : perc.entrySet()) {
                    if (e.getValue() <= 0) continue;
                    out.append(String.format("  %s = %.2f -> %s\n", e.getKey(), e.getValue(), mapPercentageToTerm(e.getValue())));
                }

                out.append("\nStep 5: Combine description\n");
                // order components
                List<Map.Entry<String, Double>> comps = new ArrayList<>(perc.entrySet());
                comps.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
                StringBuilder compSb = new StringBuilder();
                for (int i = 0; i < comps.size(); i++) {
                    String name = comps.get(i).getKey();
                    double pct = comps.get(i).getValue();
                    if (pct <= 0) continue;
                    if (i == 0) compSb.append(name.toUpperCase());
                    else {
                        String term = mapPercentageToTerm(pct);
                        String displayName = name;
                        double sandPct = perc.getOrDefault("sand", 0.0);
                        double siltPct = perc.getOrDefault("silt", 0.0);
                        double clayPct = perc.getOrDefault("clay", 0.0);
                        if (name.equals("sand") && sandPct <= siltPct && sandPct <= clayPct) displayName = "fine sand";
                        compSb.append(String.format(", %s %s", term.toLowerCase(), displayName));
                    }
                }

                String finalDesc = String.format("%s %s %s", mapColorCode(vc.colorCode),
                        condLow.equals(condHigh) ? condLow.toLowerCase() : (condLow + " / " + condHigh).toLowerCase(),
                        compSb.toString());

                out.append(String.format("Final Description:\n%s\n", finalDesc.trim()));

                txtResult.setText(out.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                txtResult.setText("Failed to generate analysis: " + ex.getMessage());
            }
        }
    }

    private void setupTables() {
        if (colPercRange != null) colPercRange.setCellValueFactory(c -> c.getValue().rangeProperty());
        if (colTerm != null) colTerm.setCellValueFactory(c -> c.getValue().termProperty());

        if (colTotalRange2 != null) colTotalRange2.setCellValueFactory(c -> c.getValue().rangeProperty());
        if (colCondition2 != null) colCondition2.setCellValueFactory(c -> c.getValue().conditionProperty());

        if (colTotalRange3 != null) colTotalRange3.setCellValueFactory(c -> c.getValue().rangeProperty());
        if (colCondition3 != null) colCondition3.setCellValueFactory(c -> c.getValue().conditionProperty());

        ObservableList<PercentTermRow> pctRows = FXCollections.observableArrayList();
        pctRows.add(new PercentTermRow("<5%", mapPercentageToTerm(2)));
        pctRows.add(new PercentTermRow("5-10%", mapPercentageToTerm(7)));
        pctRows.add(new PercentTermRow("10-25%", mapPercentageToTerm(17)));
        pctRows.add(new PercentTermRow("25-45%", mapPercentageToTerm(35)));
        pctRows.add(new PercentTermRow(">45%", mapPercentageToTerm(70)));
        if (tblPercentTerms != null) tblPercentTerms.setItems(pctRows);

        ObservableList<TotalSptRow> t2 = FXCollections.observableArrayList();
        t2.add(new TotalSptRow("0-4", mapSptToCondition(2, true)));
        t2.add(new TotalSptRow("5-8", mapSptToCondition(6, true)));
        t2.add(new TotalSptRow("9-16", mapSptToCondition(12, true)));
        t2.add(new TotalSptRow("17-30", mapSptToCondition(23, true)));
        t2.add(new TotalSptRow("31-50", mapSptToCondition(40, true)));
        t2.add(new TotalSptRow(">50", mapSptToCondition(60, true)));
        if (tblTable2 != null) tblTable2.setItems(t2);

        ObservableList<TotalSptRow> t3 = FXCollections.observableArrayList();
        t3.add(new TotalSptRow("0-3", mapSptToCondition(1, false)));
        t3.add(new TotalSptRow("4-6", mapSptToCondition(5, false)));
        t3.add(new TotalSptRow("7-11", mapSptToCondition(9, false)));
        t3.add(new TotalSptRow("12-20", mapSptToCondition(16, false)));
        t3.add(new TotalSptRow(">20", mapSptToCondition(25, false)));
        if (tblTable3 != null) tblTable3.setItems(t3);
    }

    public static class PercentTermRow {
        private final javafx.beans.property.SimpleStringProperty range;
        private final javafx.beans.property.SimpleStringProperty term;
        public PercentTermRow(String range, String term) {
            this.range = new javafx.beans.property.SimpleStringProperty(range);
            this.term = new javafx.beans.property.SimpleStringProperty(term);
        }
        public javafx.beans.property.StringProperty rangeProperty() { return range; }
        public javafx.beans.property.StringProperty termProperty() { return term; }
    }

    public static class TotalSptRow {
        private final javafx.beans.property.SimpleStringProperty range;
        private final javafx.beans.property.SimpleStringProperty condition;
        public TotalSptRow(String range, String condition) {
            this.range = new javafx.beans.property.SimpleStringProperty(range);
            this.condition = new javafx.beans.property.SimpleStringProperty(condition);
        }
        public javafx.beans.property.StringProperty rangeProperty() { return range; }
        public javafx.beans.property.StringProperty conditionProperty() { return condition; }
    }

    @FXML
    private void back(ActionEvent e) {
        try {
            FXMLLoader f = new FXMLLoader(getClass().getResource("/com/example/sptdataanalysisandreportingtool/rawdata-view.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) e.getSource()).getScene().getWindow();
            stage.setScene(new Scene(f.load()));
        } catch (Exception ex) {
            ex.printStackTrace();
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
            if (val <= 4) return "Very Loose";
            if (val <= 10) return "Loose";
            if (val <= 25) return "Medium Dense";
            if (val <= 50) return "Dense";
            return "Very Dense";
        } else {
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
}
