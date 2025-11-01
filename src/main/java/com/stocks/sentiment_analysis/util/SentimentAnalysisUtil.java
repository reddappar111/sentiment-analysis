package com.stocks.sentiment_analysis.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashMap;
import java.util.Map;

public class SentimentAnalysisUtil {
    // Determine sentiment label based on average score
    public static String determineSentimentLabel(double score, Map<String, String> definitions) {
        for (var entry : definitions.entrySet()) {
            String condition = entry.getKey().replace("x", String.valueOf(score));
            if (evaluateCondition(score,condition)) {
                return entry.getValue();
            }
        }
        return "Unknown";
    }
    // Parse sentiment score definition
    public static Map<String, String> parseSentimentScoreDefinition(String definition) {
        Map<String, String> definitions = new HashMap<>();
        String[] parts = definition.split(";");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            definitions.put(key, value);
        }
        return definitions;
    }
    public static boolean evaluateCondition(double x, String condition) {
        String[] parts = condition.split(" ");
        double lowerBound = Double.NEGATIVE_INFINITY;
        double upperBound = Double.POSITIVE_INFINITY;
        double value = Double.POSITIVE_INFINITY;
        if (parts.length == 3) {
            value = Double.parseDouble(parts[2]);
            return switch (parts[1]) {
                case "<=" -> x <= value;
                case "<" -> x < value;
                case ">=" -> x >= value;
                case ">" -> x > value;
                default -> false;
            };
        } else if (parts.length == 5) {
            lowerBound = Double.parseDouble(parts[0]);
            upperBound = Double.parseDouble(parts[4]);
            return switch (parts[1]) {
                case "<" -> lowerBound < x;
                case "<=" -> lowerBound <= x;
                default -> false;
            } && switch (parts[3]) {
                case "<" -> x < upperBound;
                case "<=" -> x <= upperBound;
                default -> false;
            };
        }

        return false;
    }
    public static JsonNode transformJson(JsonNode inputNode) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode inputFeed = (ArrayNode) inputNode.get("feed");
        ArrayNode outputFeed = mapper.createArrayNode();
        String sentimentScoreDefinition = inputNode.get("sentiment_score_definition").asText();
        Map<String, String> sentimentDefinitions = SentimentAnalysisUtil.parseSentimentScoreDefinition(sentimentScoreDefinition);
        for (JsonNode item : inputFeed) {
            ObjectNode newItem = mapper.createObjectNode();

            newItem.put("title", item.get("title").asText());
            newItem.put("url", item.get("url").asText());
            newItem.put("time_published", item.get("time_published").asText());
            newItem.put("summary", item.get("summary").asText());
            newItem.put("overall_sentiment_score", item.get("overall_sentiment_score").asText());
            newItem.put("overall_sentiment_label", determineSentimentLabel(Double.parseDouble(item.get("overall_sentiment_score").asText()),sentimentDefinitions));
            ArrayNode tickerSentiment = (ArrayNode) item.get("ticker_sentiment");
            ArrayNode newTickerSentiment = mapper.createArrayNode();

            for (JsonNode ticker : tickerSentiment) {
                ObjectNode newTicker = mapper.createObjectNode();
                newTicker.put("ticker", ticker.get("ticker").asText());
                newTicker.put("ticker_sentiment_label", ticker.get("ticker_sentiment_label").asText());
                newTickerSentiment.add(newTicker);
            }

            newItem.set("ticker_sentiment", newTickerSentiment);
            outputFeed.add(newItem);
        }

        ObjectNode outputObject = mapper.createObjectNode();
        outputObject.set("feed", outputFeed);

        return outputObject;
    }

}
