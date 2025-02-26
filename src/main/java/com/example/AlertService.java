package com.example;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlertService {
    private static final String TWILIO_API_URL = "https://api.twilio.com/2010-04-01/Accounts/YOUR_ACCOUNT_SID/Messages.json";
    private static final String TWILIO_AUTH = "YOUR_AUTH_TOKEN";
    private static final String FROM_PHONE = "+1234567890"; // Your Twilio number
    private static final String TO_PHONE = "+0987654321"; // User's phone number

    private final RestTemplate restTemplate;

    public AlertService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendAlert(String message) {
        Map<String, String> request = new HashMap<>();
        request.put("From", FROM_PHONE);
        request.put("To", TO_PHONE);
        request.put("Body", message);

        restTemplate.postForEntity(TWILIO_API_URL, request, String.class);
    }
}
