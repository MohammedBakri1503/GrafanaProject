package com.example;

import com.example.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class StockService {
    private static final String ALPHA_VANTAGE_API_KEY = "YOUR_API_KEY"; // Replace with your key
    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=5min&apikey=" + ALPHA_VANTAGE_API_KEY;

    private final RestTemplate restTemplate;
    private final AlertService alertService;

    public StockService(RestTemplate restTemplate, AlertService alertService) {
        this.restTemplate = restTemplate;
        this.alertService = alertService;
    }

    public Map<String, Object> fetchStockData(String symbol) {
        String url = String.format(ALPHA_VANTAGE_URL, symbol);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // Extract latest stock price
        if (response != null && response.containsKey("Time Series (5min)")) {
            Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (5min)");
            String latestTime = timeSeries.keySet().iterator().next();
            Map<String, Object> latestData = (Map<String, Object>) timeSeries.get(latestTime);

            // Send data to AI anomaly detection
            boolean isAnomaly = sendToAIModel(latestData);

            if (isAnomaly) {
                alertService.sendAlert("Anomaly detected in stock: " + symbol);
            }

            return latestData;
        }

        return new HashMap<>();
    }

    private boolean sendToAIModel(Map<String, Object> stockData) {
        String aiUrl = "http://localhost:5000/detect"; // Flask AI model URL
        Map<String, Boolean> response = restTemplate.postForObject(aiUrl, stockData, Map.class);
        return response != null && Boolean.TRUE.equals(response.get("anomaly"));
    }
}
