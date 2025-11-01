package com.stocks.sentiment_analysis.model;

public class SearchParams {
    private String tickers;
    private String timeFrom;
    private String timeTo;
    private String topics;

    // Getters and Setters
    public String getTickers() {
        return tickers;
    }

    public void setTickers(String tickers) {
        this.tickers = tickers;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }
}

