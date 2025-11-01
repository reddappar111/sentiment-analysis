package com.stocks.sentiment_analysis.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class OverAllSentimentScore {
    private Double overAllSentimentScore;
    private String sentimentLabel;
}
