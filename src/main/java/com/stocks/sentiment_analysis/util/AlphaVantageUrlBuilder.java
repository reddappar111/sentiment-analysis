package com.stocks.sentiment_analysis.util;

import com.stocks.sentiment_analysis.model.SearchParams;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AlphaVantageUrlBuilder {

    private static final String BASE_URL = "https://www.alphavantage.co/query?function=NEWS_SENTIMENT&limit=50&apikey=BH0R4TUR7N896XP1";

    public static String buildUrl(SearchParams searchParams) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        Optional.ofNullable(searchParams.getTickers())
                .filter(tickers -> !tickers.isEmpty())
                .ifPresent(tickers -> urlBuilder.append("&tickers=").append(String.join(",", tickers)));

        Optional.ofNullable(searchParams.getTimeFrom())
                .filter(timeFrom -> !timeFrom.isEmpty())
                .map(AlphaVantageUrlBuilder::convertTimeFormat)
                .ifPresent(dateTime -> urlBuilder.append("&time_from=").append(dateTime));

        Optional.ofNullable(searchParams.getTimeTo())
                .filter(timeTo -> !timeTo.isEmpty())
                .map(AlphaVantageUrlBuilder::convertTimeFormat)
                .ifPresent(dateTime -> urlBuilder.append("&time_to=").append(dateTime));

        Optional.ofNullable(searchParams.getTopics())
                .filter(topics -> !topics.isEmpty())
                .ifPresent(topics -> urlBuilder.append("&topics=").append(String.join(",", topics)));

        return urlBuilder.toString();
    }

    private static String convertTimeFormat(String timeFromTo) {
        // Define the input and output date-time formats
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");

        // Parse the input date-time string
        LocalDateTime dateTime = LocalDateTime.parse(timeFromTo, inputFormatter);
        return dateTime.format(outputFormatter).toString();
    }
}

