import java.time.LocalDate;

public class CropPlot {
    // 1. Private Fields
    private String plotId;
    private String cropName;
    private String address;
    private double landSize;
    private LocalDate plantingDate;

    // 2. Default Constructor
    public CropPlot() {}

    // 3. Multi-Argument Constructor
    // Added 'address' and 'landSize' so the object is fully initialized upon creation
    public CropPlot(String plotId, String cropName, String address, double landSize, LocalDate plantingDate) {
        this.plotId = plotId;
        this.cropName = cropName;
        this.address = address;
        this.landSize = landSize;
        this.plantingDate = plantingDate;
    }

    // 4. Getters and Setters
    // Organized logically to ensure every field can be accessed and modified
    
    public String getPlotId() { return plotId; }
    public void setPlotId(String plotId) { this.plotId = plotId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLandSize() { return landSize; }
    public void setLandSize(double landSize) { this.landSize = landSize; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    // 5. Utility Methods
    @Override
    public String toString() {
        return "Plot [" + plotId + "] - Crop: " + cropName + ", Size: " + landSize + " acres";
    }
}