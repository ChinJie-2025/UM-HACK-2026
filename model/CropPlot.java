import java.time.LocalDate;

public class CropPlot {
    private String plotId;
    private String cropName;
    private double landSize;
    private LocalDate plantingDate;

    public CropPlot() {}

    public CropPlot(String plotId, String cropName, double landSize, LocalDate plantingDate) {
        this.plotId = plotId;
        this.cropName = cropName;
        this.landSize = landSize;
        this.plantingDate = plantingDate;
    }

    // Getters and Setters
    public String getPlotId() { return plotId; }
    public String getCropName() { return cropName; }
    public LocalDate getPlantingDate() { return plantingDate; }
}