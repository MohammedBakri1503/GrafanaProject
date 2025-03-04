/*package testAPI;

import com.example.controller.AlertService;
import com.example.controller.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;  // ✅ Mock WebClient.Builder

    @Mock
    private WebClient webClient; // ✅ Mock WebClient

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AlertService alertService;

    private StockService stockService; // ✅ No @InjectMocks, manually initializing

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ✅ Ensure WebClient.Builder is properly mocked and injected
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // ✅ Mock WebClient call flow
        when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // ✅ Fix ambiguous bodyToMono()
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(Map.of("Time Series (5min)", Map.of())));

        // ✅ Initialize StockService with WebClient.Builder instead of WebClient
        stockService = new StockService(webClientBuilder, alertService);
    }

    @Test
    void testFetchStockData_NoDataAvailable() {
        // Call the method
        Map<String, Object> result = stockService.fetchStockData("AAPL");

        // Assertions
        assertNotNull(result);
        assertTrue(result.containsKey("error"));
        assertEquals("No stock data available", result.get("error"));
    }
}
*/