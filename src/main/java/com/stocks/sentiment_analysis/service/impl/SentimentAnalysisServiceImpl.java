package com.stocks.sentiment_analysis.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.stocks.sentiment_analysis.client.AlphaVantageClient;
import com.stocks.sentiment_analysis.model.OverAllSentimentScore;
import com.stocks.sentiment_analysis.model.SearchParams;
import com.stocks.sentiment_analysis.model.SentimentData;
import com.stocks.sentiment_analysis.service.SentimentAnalysisService;
import com.stocks.sentiment_analysis.util.SentimentAnalysisUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService {

    private final AlphaVantageClient alphaVantageClient;
    private final ObjectMapper objectMapper;

    public SentimentAnalysisServiceImpl(AlphaVantageClient alphaVantageClient, ObjectMapper objectMapper) {
        this.alphaVantageClient = alphaVantageClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void getAverageSentimentData(SearchParams searchParams, Model model) throws JsonProcessingException {
        Map<String, String> averageScores = new HashMap<>();
        ArrayNode arrayNode = objectMapper.createArrayNode();

        try {
            String response = alphaVantageClient.getNewsSentiment(searchParams);
            AtomicReference<JsonNode> rootNode = new AtomicReference<>(objectMapper.readTree(response));
            readFile(rootNode);
            // Process the rootNode to calculate average scores and determine sentiment label
            // Read sentiment score definitions
            String sentimentScoreDefinition = rootNode.get().get("sentiment_score_definition").asText();
            Map<String, String> sentimentDefinitions = SentimentAnalysisUtil.parseSentimentScoreDefinition(sentimentScoreDefinition);
            // Extract the feed array
            JsonNode feedArray = rootNode.get().get("feed");
            // Map to store the sum of scores and count for each ticker
            Map<String, TickerData> tickerScores = new HashMap<>();
            // Iterate through each feed object and extract ticker_sentiment arrays

            extractSentiment(arrayNode, sentimentDefinitions, feedArray, tickerScores);
            OverAllSentimentScore overAllSentimentScore = getOverAllAverageSentimentScore(feedArray, sentimentDefinitions);
            // Print the results
            //averageScores.forEach((tickerName, avgScore) -> System.out.println(tickerName + ": " + avgScore));
            ObjectMapper objectMapper = new ObjectMapper();
            List<SentimentData> sentimentDataList = null;
            try {
                sentimentDataList = objectMapper.readValue(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(arrayNode), new TypeReference<List<SentimentData>>() {});
            } catch (Exception e) {
                e.printStackTrace();
            }
            Comparator<SentimentData> customComparator = getSentimentDataComparator();
            // Sort the list using the custom comparator
            boolean tickerPresent = searchParams.getTickers() != null;
            model.addAttribute("overAllSentimentScore",overAllSentimentScore);
            model.addAttribute("sentimentDataList", getSentimentData(searchParams, sentimentDataList, customComparator, tickerPresent));
            model.addAttribute("sentimentMainDataList", getMainSentimentData(searchParams, sentimentDataList, customComparator, tickerPresent));
            model.addAttribute("feedsData",SentimentAnalysisUtil.transformJson(rootNode.get()));
        } catch (IOException e) {
            e.printStackTrace();
            //return Collections.emptyList();
        }
    }

    private OverAllSentimentScore getOverAllAverageSentimentScore(JsonNode feedArray, Map<String, String> sentimentDefinitions) {
        Double overAllSentimentScore = 0.0;
        for (JsonNode feed : feedArray) {
            JsonNode tickerSentimentArray = feed.get("ticker_sentiment");
            overAllSentimentScore = overAllSentimentScore + feed.get("overall_sentiment_score").asDouble();
        }
        overAllSentimentScore = Double.parseDouble(String.format("%.6f", overAllSentimentScore / feedArray.size()));
        String sentimentLabel = SentimentAnalysisUtil.determineSentimentLabel(overAllSentimentScore, sentimentDefinitions);
        OverAllSentimentScore overAllSentimentScoreObj = new OverAllSentimentScore();
        overAllSentimentScoreObj.setOverAllSentimentScore(overAllSentimentScore);
        overAllSentimentScoreObj.setSentimentLabel(sentimentLabel);
        return overAllSentimentScoreObj;
    }

    private Object getMainSentimentData(SearchParams searchParams, List<SentimentData> sentimentDataList, Comparator<SentimentData> customComparator, boolean tickerPresent) {
        return sentimentDataList.stream().filter(data -> data.getTicker().equalsIgnoreCase(searchParams.getTickers())).collect(Collectors.toList());
    }

    private void extractSentiment(ArrayNode arrayNode, Map<String, String> sentimentDefinitions, JsonNode feedArray, Map<String, TickerData> tickerScores) {
        for (JsonNode feed : feedArray) {
            JsonNode tickerSentimentArray = feed.get("ticker_sentiment");
            if (tickerSentimentArray != null) {
                for (JsonNode sentiment : tickerSentimentArray) {
                    String tickerLabel = sentiment.get("ticker").asText();
                    double score = sentiment.get("ticker_sentiment_score").asDouble();

                    tickerScores.merge(tickerLabel, new TickerData(score, 1), (oldData, newData) ->
                            new TickerData(oldData.totalScore() + newData.totalScore(), oldData.count() + newData.count())
                    );
                }
            }
        }

        for (var entry : tickerScores.entrySet()) {
            String tickerName = entry.getKey();
            TickerData data = entry.getValue();
            double averageScore = data.totalScore() / data.count();
            String sentimentLabel = SentimentAnalysisUtil.determineSentimentLabel(averageScore, sentimentDefinitions);
            ObjectNode sentimentNode = objectMapper.createObjectNode();
            sentimentNode.put("ticker", tickerName);
            sentimentNode.put("averageScore", String.format("%.6f", averageScore));
            sentimentNode.put("sentimentLabel", sentimentLabel);
            arrayNode.add(sentimentNode);
        }
    }

    private void readFile(AtomicReference<JsonNode> rootNode) {
        // Read Sample json if Alpha Vantage API usage limit exceeds.
        Optional.ofNullable(rootNode.get().get("Information")).map(nodeObj -> {
            ClassPathResource resource = new ClassPathResource("Sample_output.json");
            File file = Optional.ofNullable(resource)
                    .map(res -> {
                        try {
                            return res.getFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new RuntimeException("File not found"));
            try {
                System.out.println("Reading data from file.");
                rootNode.set(objectMapper.readTree(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return nodeObj;
        });
    }

    private List<SentimentData> getSentimentData(SearchParams searchParams, List<SentimentData> sentimentDataList, Comparator<SentimentData> customComparator, boolean tickerPresent) {
        if (tickerPresent) {
            return /*Stream.concat(
                    sentimentDataList.stream().filter(data -> data.getTicker().equalsIgnoreCase(searchParams.getTickers())),*/
                    sentimentDataList.stream()
                            .filter(data -> !data.getTicker().equalsIgnoreCase(searchParams.getTickers()))
                            .sorted(customComparator)
            .collect(Collectors.toList());
        } else {
            return sentimentDataList.stream().sorted(customComparator).collect(Collectors.toList());
        }
    }

    private Comparator<SentimentData> getSentimentDataComparator() {
        // Custom comparator for sorting
        Comparator<SentimentData> customComparator = Comparator.comparingInt(data -> {
            switch (data.getSentimentLabel()) {
                case "Bullish":
                    return 1;
                case "Somewhat_Bullish":
                    return 2;
                case "Neutral":
                    return 3;
                case "Somewhat-Bearish":
                    return 4;
                case "Bearish":
                    return 5;
                default:
                    return 6;
            }
        });
        return customComparator;
    }

    // Using Java 17 record to store ticker data
record TickerData(double totalScore, int count) {}
}
