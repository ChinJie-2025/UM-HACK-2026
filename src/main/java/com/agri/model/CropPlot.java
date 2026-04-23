package src.main.java.com.agri.model;

import java.time.LocalDate;

public class CropPlot {
    private String plotId;
    private String cropName;
    private String address;
    private double landSize;
    private LocalDate plantingDate;
    private double plotBudget;

    // Constructor
    public CropPlot(String plotId, String cropName, String address, double landSize, LocalDate plantingDate,double plotBudget) {
        this.plotId = plotId;
        this.cropName = cropName;
        this.address = address;
        this.landSize = landSize;
        this.plantingDate = plantingDate;
        this.plotBudget = plotBudget;
    }

    // Getters and Setters
    public String getPlotId() { return plotId; }
    public void setPlotId(String plotId) { this.plotId = plotId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }


    public double getPlotBudget() { return plotBudget; }
    public void setPlotBudget(double budget) { this.plotBudget = budget; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLandSize() { return landSize; }
    public void setLandSize(double landSize) { this.landSize = landSize; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    @Override
    public String toString() {
        return String.format("Plot: %s | Crop: %s | Budget: $%.2f", plotId, cropName, plotBudget);
    }
}