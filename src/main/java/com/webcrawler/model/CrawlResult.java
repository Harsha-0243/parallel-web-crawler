package com.webcrawler.model;

import java.util.List;
import java.util.Map;

/**
 * Pure data model — holds everything we want to write to the output JSON.
 * No business logic here. Just data.
 */
public class CrawlResult {

    private List<String>         visitedUrls;
    private Map<String, Integer> wordFrequencies;
    private long                 crawlDurationMs;
    private int                  totalPagesCrawled;

    public CrawlResult(List<String> visitedUrls,
                       Map<String, Integer> wordFrequencies,
                       long crawlDurationMs) {
        this.visitedUrls       = visitedUrls;
        this.wordFrequencies   = wordFrequencies;
        this.crawlDurationMs   = crawlDurationMs;
        this.totalPagesCrawled = visitedUrls.size();
    }

    public List<String>         getVisitedUrls()      { return visitedUrls; }
    public Map<String, Integer> getWordFrequencies()  { return wordFrequencies; }
    public long                 getCrawlDurationMs()  { return crawlDurationMs; }
    public int                  getTotalPagesCrawled(){ return totalPagesCrawled; }
}