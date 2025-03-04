package com.example.controller;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.Optional;

@Service
public class StockService {

    private final WebClient webClient;
    private final AlertService alertService;

    private static final String TWELVE_DATA_API_KEY = "b5e8046abc3f4817aba2c5a3f46b678b"; // 🔹 Replace with your Twelve Data API key
    private static final String AI_MODEL_URL = "http://127.0.0.1:5000/detect"; // Flask AI Model URL

    public StockService(WebClient.Builder webClientBuilder, AlertService alertService) {
        this.webClient = webClientBuilder.baseUrl("https://api.twelvedata.com").build();
        this.alertService = alertService;
    }

    /**
     * Fetches stock data from Twelve Data API.
     */
    public Map<String, Object> fetchStockDataFromTwelveData(String symbol) {
        String url = String.format("/time_series?symbol=%s&interval=5min&apikey=%s&outputsize=1",
                symbol, TWELVE_DATA_API_KEY);
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // Blocking call for simplicity
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch stock data");
        }
    }

    /**
     * Processes the fetched stock data, extracts price, checks for anomalies, and sends alerts if needed.
     */
    public Map<String, Object> processStockData(String symbol, Map<String, Object> stockData) {
        if (stockData.containsKey("error")) {
            return stockData; // Return error if API request fails
        }

        if (!"ok".equals(stockData.get("status"))) {
            return Map.of("error", "Invalid API response");
        }

        Optional<Double> latestPrice = extractLatestStockPrice(stockData);
        if (latestPrice.isEmpty()) {
            return Map.of("error", "No stock data available");
        }

        boolean isAnomaly = checkAnomaly(latestPrice.get());
        if (isAnomaly) {
            sendAnomalyAlert(symbol, latestPrice.get());
        }

        return stockData;
    }

    /**
     * Extracts the latest stock closing price from Twelve Data API response.
     */
    private Optional<Double> extractLatestStockPrice(Map<String, Object> stockData) {
        if (!stockData.containsKey("values")) {
            return Optional.empty();
        }

        var valuesList = (java.util.List<Map<String, Object>>) stockData.get("values");
        if (valuesList.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> latestData = valuesList.get(0); // Get the latest entry

        if (!latestData.containsKey("close")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(latestData.get("close").toString()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Sends the stock price to the AI model for anomaly detection.
     */
    public boolean checkAnomaly(double price) {
        Map<String, Object> request = Map.of("close", price);

        try {
            Map<String, Boolean> response = webClient.post()
                    .uri(AI_MODEL_URL)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null && response.getOrDefault("anomaly", false);
        } catch (Exception e) {
            return false; // If the AI service is down, assume no anomaly
        }
    }

    /**
     * Sends an alert when an anomaly is detected.
     */
    private void sendAnomalyAlert(String symbol, double price) {
        String alertMessage = "⚠️ Anomaly detected for " + symbol + "\nPrice: $" + price;
        alertService.sendAlert(alertMessage);
    }
}
