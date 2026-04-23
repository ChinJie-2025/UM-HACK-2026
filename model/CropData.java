public class CropData {
    private String name;
    private double expectedYield;  // tons per hectare
    private double marketPrice;    // price per ton
    private double waterReq;       // liters per day/cycle
    private String season;         // "Dry", "Monsoon", etc.

    public CropData() {}

    // Getters and Setters
    public String getName() { return name; }
    public double getMarketPrice() { return marketPrice; }
    public void setMarketPrice(double marketPrice) { this.marketPrice = marketPrice; }
}