package com.webcrawler.crawler;

import com.google.inject.Inject;
import com.webcrawler.config.CrawlerConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class PageFetcher {

    private final CrawlerConfig config;

    @Inject
    public PageFetcher(CrawlerConfig config) {
        this.config = config;
    }

    public Optional<Document> fetch(String url) {

        // Basic URL format validation only — filtering is done by WebCrawler
        if (!isValidUrl(url)) return Optional.empty();

        try {
            Thread.sleep(200); // Polite delay — avoids HTTP 429 rate limiting

            Document doc = Jsoup.connect(url)
                    .timeout(config.getTimeoutMs())
                    .maxBodySize(2 * 1024 * 1024)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                             + "AppleWebKit/537.36 (KHTML, like Gecko) "
                             + "Chrome/120.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com")
                    .get();

            System.out.println("  [FETCHED] " + url);
            return Optional.of(doc);

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("  [TIMEOUT] " + url);
        } catch (org.jsoup.HttpStatusException e) {
            System.out.println("  [HTTP-" + e.getStatusCode() + "] " + url);
        } catch (javax.net.ssl.SSLException e) {
            System.out.println("  [SSL-ERROR] " + url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("  [ERROR] " + url + " → " + e.getMessage());
        }

        return Optional.empty();
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) return false;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return false;
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}