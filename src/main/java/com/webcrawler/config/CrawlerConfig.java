package com.webcrawler.config;

import java.util.List;

/**
 * Holds all configuration loaded from crawler.json
 * No hardcoded values anywhere in the project — everything comes from here.
 */
public class CrawlerConfig {

    private List<String> seedUrls;
    private int maxDepth;
    private int parallelism;
    private int timeoutMs;
    private int topN;
    private String resultPath;
    private List<String> ignoredUrls;
    private List<String> ignoredWords;

    // ---------- Getters ----------

    public List<String> getSeedUrls()    { return seedUrls; }
    public int getMaxDepth()             { return maxDepth; }
    public int getParallelism()          { return parallelism; }
    public int getTimeoutMs()            { return timeoutMs; }
    public int getTopN()                 { return topN; }
    public String getResultPath()        { return resultPath; }
    public List<String> getIgnoredUrls() { return ignoredUrls; }
    public List<String> getIgnoredWords(){ return ignoredWords; }

    // ---------- Setters ----------

    public void setSeedUrls(List<String> seedUrls)       { this.seedUrls = seedUrls; }
    public void setMaxDepth(int maxDepth)                 { this.maxDepth = maxDepth; }
    public void setParallelism(int parallelism)           { this.parallelism = parallelism; }
    public void setTimeoutMs(int timeoutMs)               { this.timeoutMs = timeoutMs; }
    public void setTopN(int topN)                         { this.topN = topN; }
    public void setResultPath(String resultPath)          { this.resultPath = resultPath; }
    public void setIgnoredUrls(List<String> ignoredUrls) { this.ignoredUrls = ignoredUrls; }
    public void setIgnoredWords(List<String> ignoredWords){ this.ignoredWords = ignoredWords; }
}