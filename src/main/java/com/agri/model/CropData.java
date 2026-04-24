package com.agri.model;
public class CropData {
    private String name;
    private double expectedYield;  // tons per hectare
    private double marketPrice;    // price per ton
    private double waterReq;       // liters per day/cycle
    private String season;         // "Dry", "Monsoon", etc.

    public CropData() {}

    public CropData(String name, double marketPrice, double expectedYield, double waterReq) {
        this.name = name;
        this.marketPrice = marketPrice;
        this.expectedYield = expectedYield;
        this.waterReq = waterReq;
    }

    // Getters and Setters
    public String getName() { return name; }
    public double getMarketPrice() { return marketPrice; }
    public void setMarketPrice(double marketPrice) { this.marketPrice = marketPrice; }
}