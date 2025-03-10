package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/stocks")
public class StockDataController {

    @Autowired
    private StockService stockService;

    @GetMapping("/{symbol}")
    public Map<String, Object> getStockData(@PathVariable String symbol) {
        Map<String, Object> rawStockData = stockService.fetchStockDataFromTwelveData(symbol);
        return stockService.processStockData(symbol, rawStockData);
    }
}
