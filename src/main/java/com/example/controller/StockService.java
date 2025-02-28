package com.example.controller;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class StockService {

    private final WebClient webClient;
    private final AlertService alertService;
    private static final String ALPHA_VANTAGE_API_KEY = "YOUR_API_KEY"; // Replace with your API key
    private static final String AI_MODEL_URL = "http://127.0.0.1:5000/detect"; // Flask AI Model URL

    public StockService(WebClient.Builder webClientBuilder, AlertService alertService) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.alertService = alertService;
    }

    public Map<String, Object> fetchStockData(String symbol) {
        String url = String.format("/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=5min&apikey=%s", symbol, ALPHA_VANTAGE_API_KEY);

        // Fetch stock data from Alpha Vantage
        Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // Blocking call for simplicity (consider using async)

        // Extract latest stock price
        if (response != null && response.containsKey("Time Series (5min)")) {
            Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (5min)");
            String latestTime = timeSeries.keySet().iterator().next(); // Get latest timestamp
            Map<String, Object> latestData = (Map<String, Object>) timeSeries.get(latestTime);

            double latestPrice = Double.parseDouble(latestData.get("1. open").toString());

            // Send stock price to AI model
            boolean isAnomaly = checkAnomaly(latestPrice);

            if (!isAnomaly) {
                String alertMessage = "⚠️ Anomaly detected for " + symbol + " at " + latestTime +
                        "\nPrice: " + latestPrice;
                alertService.sendAlert(alertMessage);
            }

            return latestData;
        }

        return Map.of("error", "No stock data available");
    }

    private boolean checkAnomaly(double price) {
        Map<String, Object> request = Map.of("1. open", price); // Sending stock price to AI model

        Map<String, Boolean> response = webClient.post()
                .uri(AI_MODEL_URL)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // Blocking call for simplicity

        return response != null && response.get("anomaly"); // Returns true if anomaly detected
    }
}
