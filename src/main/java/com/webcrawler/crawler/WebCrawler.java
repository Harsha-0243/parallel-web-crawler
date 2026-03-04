package com.webcrawler.crawler;

import com.google.inject.Inject;
import com.webcrawler.config.CrawlerConfig;
import com.webcrawler.model.CrawlResult;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {

    private final CrawlerConfig config;
    private final PageFetcher   pageFetcher;
    private final WordCounter   wordCounter;

    private final Set<String>  visitedUrls = ConcurrentHashMap.newKeySet();
    private final List<String> visitedList = Collections.synchronizedList(new ArrayList<>());

    private final AtomicInteger pendingTasks = new AtomicInteger(0);
    private final Object        lock         = new Object();

    private static final int MAX_PAGES = 50;

    // ✅ WHITELIST: Only crawl these domains — everything else is rejected
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
        "en.wikipedia.org"
    );

    @Inject
    public WebCrawler(CrawlerConfig config,
                      PageFetcher pageFetcher,
                      WordCounter wordCounter) {
        this.config      = config;
        this.pageFetcher = pageFetcher;
        this.wordCounter = wordCounter;
    }

    public CrawlResult start() {
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(config.getParallelism());

        System.out.println("\n==============================");
        System.out.println(" CRAWLER STARTING");
        System.out.println(" Seed URLs   : " + config.getSeedUrls().size());
        System.out.println(" Max Depth   : " + config.getMaxDepth());
        System.out.println(" Parallelism : " + config.getParallelism() + " threads");
        System.out.println(" Max Pages   : " + MAX_PAGES);
        System.out.println(" Allowed     : " + ALLOWED_DOMAINS);
        System.out.println("==============================\n");

        for (String seedUrl : config.getSeedUrls()) {
            submitTask(executor, seedUrl, 0);
        }

        waitUntilAllDone();
        executor.shutdown();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n==============================");
        System.out.println(" CRAWL COMPLETE");
        System.out.println(" Pages crawled : " + visitedList.size());
        System.out.println(" Duration      : " + duration + " ms");
        System.out.println("==============================\n");

        return new CrawlResult(
                new ArrayList<>(visitedList),
                wordCounter.getWordCountsAsMap(),
                duration
        );
    }

    private void submitTask(ExecutorService executor, String url, int depth) {
        pendingTasks.incrementAndGet();
        executor.submit(() -> {
            try {
                crawlPage(url, depth, executor);
            } finally {
                int remaining = pendingTasks.decrementAndGet();
                if (remaining == 0) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            }
        });
    }

    private void crawlPage(String url, int depth, ExecutorService executor) {

        if (!isWorthCrawling(url, depth)) return;

        if (!visitedUrls.add(url)) return;

        if (visitedList.size() >= MAX_PAGES) return;

        visitedList.add(url);
        System.out.println("[Depth " + depth + "] Crawling: " + url);

        Optional<Document> docOpt = pageFetcher.fetch(url);
        if (docOpt.isEmpty()) return;

        Document doc = docOpt.get();

        String text = doc.body() != null ? doc.body().text() : "";
        wordCounter.countWords(text);

        if (depth >= config.getMaxDepth()) return;

        Elements links = doc.select("a[href]");
        System.out.println("  → Found " + links.size() + " links on: " + url);

        for (Element link : links) {
            if (visitedList.size() >= MAX_PAGES) break;

            String childUrl = link.absUrl("href");
            if (childUrl.isBlank()) continue;
            if (visitedUrls.contains(childUrl)) continue;

            submitTask(executor, childUrl, depth + 1);
        }
    }

    private boolean isWorthCrawling(String url, int depth) {

        if (depth > config.getMaxDepth()) return false;

        if (!url.startsWith("http://") && !url.startsWith("https://")) return false;

        // ✅ WHITELIST CHECK: reject any URL not from an allowed domain
        boolean domainAllowed = ALLOWED_DOMAINS.stream()
                .anyMatch(domain -> url.contains(domain));

        if (!domainAllowed) {
            System.out.println("  [SKIP-DOMAIN] " + url);
            return false;
        }

        // ✅ PATTERN BLACKLIST: reject navigation/meta/file URLs
        if (config.getIgnoredUrls() != null) {
            String lowerUrl = url.toLowerCase();
            for (String pattern : config.getIgnoredUrls()) {
                if (lowerUrl.contains(pattern.toLowerCase())) {
                    System.out.println("  [SKIP] " + url);
                    return false;
                }
            }
        }

        return true;
    }

    private void waitUntilAllDone() {
        synchronized (lock) {
            while (pendingTasks.get() > 0) {
                try {
                    lock.wait(2000);
                    if (pendingTasks.get() > 0) {
                        System.out.println("  [waiting] pending tasks: "
                                + pendingTasks.get()
                                + " | pages crawled: " + visitedList.size());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}