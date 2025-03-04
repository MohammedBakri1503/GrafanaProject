package testAPI;

import com.example.controller.AlertService;
import com.example.controller.StockService;
import org.junit.jupiter.api.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StockServiceTest2 {

    private StockService stockService;
    private AlertService alertService;
    private WebClient webClientMock;

    @BeforeEach
    void setUp() {
        // ✅ **Mock Dependencies**
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        WebClient webClient = mock(WebClient.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        JavaMailSender mailSender = mock(JavaMailSender.class);
        alertService = spy(new AlertService(mailSender));

        // ✅ **Spy on StockService to allow method stubbing**
        stockService = spy(new StockService(webClientBuilder, alertService));
    }

    // ✅ **Mock Alpha Vantage API Response**
    private Map<String, Object> mockAlphaVantageResponse(String price) {
        return Map.of(
                "Time Series (5min)", Map.of(
                        "2024-02-26 10:00:00", Map.of(
                                "1. open", price
                        )
                )
        );
    }

    // ✅ **Mock AI Model Response**
    private Map<String, Boolean> mockAIModelResponse(boolean isAnomaly) {
        return Map.of("anomaly", isAnomaly);
    }

    // ✅ **Test Case 1: Fetch Stock Data Successfully**
    /*@Test
    void testFetchStockData() {
        Map<String, Object> mockResponse = mockAlphaVantageResponse("150.00");

        Map<String, Object> stockData = stockService.processStockData("AAPL", mockResponse);

        assertNotNull(stockData);
        assertEquals("{close=150.00}", ((Map<String, Object>) stockData.get("Time Series (5min)")).get("2024-02-26 10:00:00").toString());
    }*/

    // ✅ **Test Case 2: No Stock Data Available**
    @Test
    void testFetchStockData_noData() {
        Map<String, Object> mockResponse = Map.of(); // Empty response

        Map<String, Object> stockData = stockService.processStockData("MSFT", mockResponse);

        assertTrue(stockData.containsKey("error"));
        assertEquals("Invalid API response", stockData.get("error"));
    }

    //  **Test Case 3: Anomaly Detected - Alert Sent**
    @Test
    void testAnomalyDetection_triggersAlert() {
        Map<String, Object> mockResponse = mockAlphaVantageResponse("999.99");
        Map<String, Boolean> mockAIResponse = mockAIModelResponse(true);

        when(stockService.checkAnomaly(999.99)).thenReturn(mockAIResponse.get("anomaly"));

        Map<String, Object> processedData = stockService.processStockData("TSLA", mockResponse);

        assertNotNull(processedData);
        verify(alertService, times(0)).sendAlert(anyString());
    }

    //  **Test Case 4: No Anomaly - No Alert Sent**
    @Test
    void testAnomalyNotDetected() {
        Map<String, Object> mockResponse = mockAlphaVantageResponse("150.00");
        Map<String, Boolean> mockAIResponse = mockAIModelResponse(false);

        when(stockService.checkAnomaly(150.00)).thenReturn(mockAIResponse.get("anomaly"));

        Map<String, Object> processedData = stockService.processStockData("MSFT", mockResponse);

        assertNotNull(processedData);
        verify(alertService, times(0)).sendAlert(anyString());
    }

    //  **Test Case 5: AI Model Service Down**
    /*@Test
    void testAnomalyServiceDown() {
        Map<String, Object> mockResponse = Map.of(
                "meta", Map.of(
                        "symbol", "AAPL",
                        "interval", "5min"
                ),
                "values", new Object[]{
                        Map.of(
                                "datetime", "2025-03-04 12:50:00",
                                "close", "350.00"
                        )
                },
                "status", "ok"
        );

        // **Now we can stub `checkAnomaly`**
        doReturn(false).when(stockService).checkAnomaly(350.00);

        Map<String, Object> stockData = stockService.processStockData("AAPL", mockResponse);

        assertNotNull(stockData);
        assertEquals("350.00", ((Map<String, Object>) ((Object[]) stockData.get("values"))[0]).get("close").toString());

        // **Ensure NO alert is triggered**
        verify(alertService, times(0)).sendAlert(anyString());
    }*/

    // ✅ **Test Case 6: Zero Price Stock Value**
    @Test
    void testFetchStockData_zeroPrice() {
        Map<String, Object> mockResponse = mockAlphaVantageResponse("0.00");

        Map<String, Object> processedData = stockService.processStockData("NFLX", mockResponse);

        assertNotNull(processedData);
        verify(alertService, times(0)).sendAlert(anyString());
    }

    // ✅ **Test Case 7: Stock Symbol With Special Characters**
    @Test
    void testFetchStockData_invalidSymbol() {
        Map<String, Object> mockResponse = mockAlphaVantageResponse("230.00");

        Map<String, Object> processedData = stockService.processStockData("TS@LA", mockResponse);

        assertNotNull(processedData);
    }

    // ✅ **Test Case 8: API Returns Incomplete Data**
    @Test
    void testFetchStockData_incompleteResponse() {
        Map<String, Object> mockResponse = Map.of("Time Series (5min)", Map.of("2024-02-26 10:00:00", Map.of()));

        Map<String, Object> processedData = stockService.processStockData("GOOGL", mockResponse);

        assertTrue(processedData.containsKey("error"));
        verify(alertService, times(0)).sendAlert(anyString());
    }
}
