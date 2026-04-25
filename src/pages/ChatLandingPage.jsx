// src/pages/ChatLandingPage.jsx

import React, { useState } from 'react';

const ChatLandingPage = () => {
  // 1. Logic State
  const [isSandboxOpen, setIsSandboxOpen] = useState(false);
  const [simulationResult, setSimulationResult] = useState(null);

  // 2. The Sandbox Action (Connects to your Spring Boot Backend)
  const runSimulation = async () => {
    // This matches your Java 'SimulationRequest' model
    const requestPayload = {
      modifiers: {
        fertilizerCost: 1.20,
        laborAvailability: 0.80
      },
      environmentalContext: "Severe Drought"
    };

    try {
        const response = await fetch('http://localhost:8080/api/crops/simulate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(requestPayload)
        });
        const data = await response.json();
        setSimulationResult(data);
    } catch (error) {
        console.error("Simulation failed:", error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col items-center p-8">
      <header className="mb-8">
        <h1 className="text-3xl font-extrabold text-green-700">AgriWise Alpha</h1>
      </header>

      {/* Main Chat Box Area (Your existing code would go here) */}
      <div className="w-full max-w-2xl bg-white shadow-xl rounded-lg p-6 mb-4">
          <p className="text-gray-500">Your AI Chat Session...</p>
      </div>

      {/* --- THE SANDBOX SECTION --- */}
      <div className="w-full max-w-2xl">
          <button 
            onClick={() => setIsSandboxOpen(!isSandboxOpen)}
            className="w-full py-3 bg-gradient-to-r from-amber-400 to-amber-600 text-white font-bold rounded-lg shadow-md hover:scale-105 transition-transform"
          >
            {isSandboxOpen ? "✖ Close Sandbox Mode" : "🛠 Open Decision Sandbox"}
          </button>

          {isSandboxOpen && (
            <div className="mt-4 p-6 bg-white border-2 border-amber-200 rounded-xl shadow-inner animate-fade-in">
              <h2 className="text-lg font-bold text-amber-800 mb-2">Scenario Simulation</h2>
              <p className="text-sm text-gray-600 mb-4">
                Testing Strategy: <b>+20% Fertilizer Cost</b> & <b>-20% Labor</b>
              </p>
              
              <button 
                onClick={runSimulation}
                className="w-full bg-green-600 hover:bg-green-700 text-white py-3 rounded-md font-medium transition-colors"
              >
                Run "What-If" Analysis
              </button>

              {simulationResult && (
                <div className="mt-6 border-t pt-4">
                  <h3 className="text-sm font-bold text-gray-500 uppercase tracking-widest">New Recommendation</h3>
                  <div className="flex justify-between items-center mt-2">
                    <span className="text-xl font-bold text-green-600">{simulationResult.recommendedCrop}</span>
                    <span className="bg-green-100 text-green-800 px-3 py-1 rounded text-sm font-semibold">
                       RM {simulationResult.economicImpact}
                    </span>
                  </div>
                  <p className="mt-2 text-xs text-gray-400 italic">
                    AI Note: {simulationResult.reasoning?.substring(0, 100)}...
                  </p>
                </div>
              )}
            </div>
          )}
      </div>
    </div>
  );
};

export default ChatLandingPage;