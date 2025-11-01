package com.stocks.sentiment_analysis.client;

import com.stocks.sentiment_analysis.model.SearchParams;
import com.stocks.sentiment_analysis.util.AlphaVantageUrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AlphaVantageClient {

    @Autowired
    RestTemplate restTemplate;



    public String getNewsSentiment(SearchParams searchParams) {
        String apiUrl = AlphaVantageUrlBuilder.buildUrl(searchParams);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                // Handle non-OK status codes
                return handleNonOkStatus((HttpStatus) response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException exception) {
            // Handle client and server errors
            return "Error: " + exception.getStatusCode() + " - " + exception.getResponseBodyAsString();
        } catch (Exception e) {
            // Handle other exceptions
            return "An unexpected error occurred: " + e.getMessage();
        }
    }

    private String handleNonOkStatus(HttpStatus status) {
        switch (status) {
            case BAD_REQUEST:
                return "Bad request. Please check the request parameters.";
            case UNAUTHORIZED:
                return "Unauthorized. Please check your API key.";
            case FORBIDDEN:
                return "Forbidden. You don't have permission to access this resource.";
            case NOT_FOUND:
                return "Not found. The requested resource could not be found.";
            case INTERNAL_SERVER_ERROR:
                return "Internal server error. Please try again later.";
            default:
                return "Unexpected status code: " + status;
        }
    }

}
