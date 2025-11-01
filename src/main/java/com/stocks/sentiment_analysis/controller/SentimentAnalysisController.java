package com.stocks.sentiment_analysis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.stocks.sentiment_analysis.model.SearchParams;
import com.stocks.sentiment_analysis.model.SentimentData;
import com.stocks.sentiment_analysis.service.SentimentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/api")
public class SentimentAnalysisController {
    @Autowired
    SentimentAnalysisService sentimentAnalysisService;

    @GetMapping("/search")
    public String searchPage(Model model) {
        model.addAttribute("ticker", "NVDA");
        model.addAttribute("currentDateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE.ofPattern("yyyyMMdd'T'HHmm")));
        return "search";
    }

    @GetMapping(value = "/sentimentData")
    public String averageSentimentData(@RequestParam(value = "tickers", required = false) String tickers,
                                       @RequestParam(value = "timeFrom", required = false) String timeFrom,
                                       @RequestParam(value = "timeTo", required = false) String timeTo,
                                       @RequestParam(value = "topics", required = false) String topics, Model model) throws JsonProcessingException {
        SearchParams searchParams = new SearchParams();
        searchParams.setTickers(tickers);
        searchParams.setTimeFrom(timeFrom);
        searchParams.setTimeTo(timeTo);
        searchParams.setTopics(topics);
        sentimentAnalysisService.getAverageSentimentData(searchParams,model);
        //model.addAttribute("sentimentDataList", sentimentDataList);
        return "sentimentData";
    }
}
