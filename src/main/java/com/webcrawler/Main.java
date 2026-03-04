package com.webcrawler;

import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.webcrawler.config.CrawlerConfig;
import com.webcrawler.crawler.WebCrawler;
import com.webcrawler.di.CrawlerModule;
import com.webcrawler.model.CrawlResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        try {
            // Step 1: Bootstrap Guice
            Injector injector = Guice.createInjector(new CrawlerModule());

            // Step 2: Get WebCrawler instance
            WebCrawler crawler = injector.getInstance(WebCrawler.class);

            // Step 3: Start crawling
            CrawlResult result = crawler.start();

            // Step 4: Get config for topN and resultPath
            CrawlerConfig config = injector.getInstance(CrawlerConfig.class);

            // Step 5: Print top words to console
            System.out.println("\n📊 TOP " + config.getTopN() + " WORDS:");
            System.out.println("─────────────────────────────");

            int rank = 1;
            for (Map.Entry<String, Integer> entry : result.getWordFrequencies().entrySet()) {
                System.out.printf("  %2d. %-25s %d%n", rank++, entry.getKey(), entry.getValue());
                if (rank > config.getTopN()) break;
            }

            // Step 6: Write JSON output
            writeJsonOutput(result, config.getResultPath());

        } catch (Exception e) {
            System.err.println("❌ Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void writeJsonOutput(CrawlResult result, String resultPath) {
        try {
            // Create output folder if it doesn't exist
            File outputFile = new File(resultPath);
            File parentDir  = outputFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();  // mkdirs creates nested folders too
                if (created) {
                    System.out.println("\n📁 Created output folder: " + parentDir.getAbsolutePath());
                } else {
                    System.err.println("⚠️  Could not create folder: " + parentDir.getAbsolutePath());
                    return;
                }
            }

            // Write JSON using FileWriter (more reliable than Files.writeString on all OS)
            String json = new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(result);

            try (FileWriter writer = new FileWriter(outputFile)) {
                writer.write(json);
            }

            System.out.println("\n✅ Results saved to : " + outputFile.getAbsolutePath());
            System.out.println("   Pages crawled    : " + result.getTotalPagesCrawled());
            System.out.println("   Duration         : " + result.getCrawlDurationMs() + " ms");

        } catch (IOException e) {
            System.err.println("❌ Failed to write output JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}