package com.webcrawler.crawler;

import com.google.inject.Inject;
import com.webcrawler.config.CrawlerConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Single Responsibility: count words from text.
 * Thread-safe: multiple threads can call countWords() simultaneously.
 *
 * KEY CONCEPTS:
 *  - ConcurrentHashMap: thread-safe map, no synchronized blocks needed
 *  - AtomicInteger: thread-safe counter using CPU-level CAS instructions
 *  - computeIfAbsent: atomically adds key if missing, then increments
 */
public class WordCounter {

    // Thread-safe map: word → count
    private final ConcurrentHashMap<String, AtomicInteger> wordCounts
            = new ConcurrentHashMap<>();

    private final Set<String> ignoredWords;

    @Inject
    public WordCounter(CrawlerConfig config) {
        // Load ignored words into a HashSet for O(1) lookup
        this.ignoredWords = config.getIgnoredWords() == null
                ? Collections.emptySet()
                : new HashSet<>(config.getIgnoredWords()
                                      .stream()
                                      .map(String::toLowerCase)
                                      .collect(Collectors.toSet()));
    }

    /**
     * Counts all words in the given text.
     * Can be called safely from multiple threads at the same time.
     */
    public void countWords(String text) {
        if (text == null || text.isBlank()) return;

        // Split on anything that's not a letter — gets clean words
        String[] tokens = text.toLowerCase().split("[^a-zA-Z]+");

        for (String word : tokens) {
            if (word.length() < 3) continue;            // Skip very short words
            if (ignoredWords.contains(word)) continue;  // Skip stop-words

            // computeIfAbsent is atomic: creates AtomicInteger(0) if key absent
            // Then incrementAndGet is atomic: thread-safe increment
            wordCounts.computeIfAbsent(word, k -> new AtomicInteger(0))
                      .incrementAndGet();
        }
    }

    /**
     * Returns the top N most frequent words, sorted descending by count.
     */
    public List<Map.Entry<String, Integer>> getTopN(int n) {
        return wordCounts.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), e.getValue().get()))
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Returns a plain Map snapshot for JSON serialization.
     */
    public Map<String, Integer> getWordCountsAsMap() {
        Map<String, Integer> result = new LinkedHashMap<>();
        getTopN(wordCounts.size()).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }
}