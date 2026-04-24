package com.agri.model;

import java.time.LocalDate;

public class CropPlot {
    private String plotId;
    private String cropName;
    private String address;
    private double landSize;
    private LocalDate plantingDate;
    private double plotBudget;
    // GPS tracking coordinates
    private double latitude;
    private double longitude;

    // Constructor
    public CropPlot(String plotId, String cropName, String address, double landSize, 
                    LocalDate plantingDate, double plotBudget, double latitude, double longitude) {
        this.plotId = plotId;
        this.cropName = cropName;
        this.address = address;
        this.landSize = landSize;
        this.plantingDate = plantingDate;
        this.plotBudget = plotBudget;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters
    public String getPlotId() { return plotId; }
    public void setPlotId(String plotId) { this.plotId = plotId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }


    // Getters and Setters for GPS
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
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