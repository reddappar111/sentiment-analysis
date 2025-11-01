package com.stocks.sentiment_analysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.stocks.sentiment_analysis.model.SearchParams;
import com.stocks.sentiment_analysis.model.SentimentData;
import org.springframework.ui.Model;

import java.util.List;

public interface SentimentAnalysisService {

    void getAverageSentimentData(SearchParams searchParams, Model model) throws JsonProcessingException;
}
