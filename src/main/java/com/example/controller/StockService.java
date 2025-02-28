package com.example.controller;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class StockService {

    private final WebClient webClient;
    private final AlertService alertService;

    public StockService(WebClient.Builder webClientBuilder, AlertService alertService) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.alertService = alertService;
    }

    public Map<String, Object> fetchStockData(String symbol) {
        String url = String.format("/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=5min&apikey=YOUR_API_KEY", symbol);

        Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // Blocking call for simplicity (consider using async)

        return response;
    }
}
