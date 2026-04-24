package com.agri.DTO;

public record AnalysisResponseDTO(
    String recommendedCrop,
    String reasoning,
    double riskScore,
    String economicImpact,
    String strategyType // Conservative, Balanced, Aggressive
) {}
