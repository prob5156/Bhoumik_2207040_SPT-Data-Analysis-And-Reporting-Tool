package com.example.sptdataanalysisandreportingtool;

public class VisualClassification {
    public final int id;
    public final String colorCode;
    public final double sandPercentage;
    public final double siltPercentage;
    public final double clayPercentage;
    public final double fromDepth;
    public final double toDepth;

    public VisualClassification(int id, String colorCode, double sandPercentage, double siltPercentage, double clayPercentage, double fromDepth, double toDepth) {
        this.id = id;
        this.colorCode = colorCode;
        this.sandPercentage = sandPercentage;
        this.siltPercentage = siltPercentage;
        this.clayPercentage = clayPercentage;
        this.fromDepth = fromDepth;
        this.toDepth = toDepth;
    }
}
