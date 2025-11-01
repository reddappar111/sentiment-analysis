package com.stocks.sentiment_analysis.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SentimentData {

    private String ticker;
    private String averageScore;
    private String sentimentLabel;

}
