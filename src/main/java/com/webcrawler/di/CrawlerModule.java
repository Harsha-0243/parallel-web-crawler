package com.webcrawler.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.gson.Gson;
import com.webcrawler.config.CrawlerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Guice Module: wires all components together.
 *
 * This is the ONLY place where we decide:
 *  "When code asks for interface X, give it implementation Y"
 *
 * Changing implementation = change ONE line here, nothing else.
 */
public class CrawlerModule extends AbstractModule {

    @Override
    protected void configure() {
        // All bindings happen here.
        // Currently PageFetcher and WordCounter are concrete classes,
        // so Guice handles them automatically via @Inject constructors.
        // Add bind(Interface.class).to(Implementation.class) if you use interfaces.
    }

    /**
     * @Provides tells Guice: "use this method to create a CrawlerConfig instance"
     * @Singleton means create it ONCE and reuse everywhere
     */
    @Provides
    @Singleton
    public CrawlerConfig provideCrawlerConfig() {
        // Load crawler.json from src/main/resources/
        try (InputStream is = getClass().getClassLoader()
                                        .getResourceAsStream("crawler.json")) {

            if (is == null) {
                throw new RuntimeException(
                    "crawler.json not found in src/main/resources/. Please create it.");
            }

            CrawlerConfig config = new Gson().fromJson(
                new InputStreamReader(is), CrawlerConfig.class
            );

            System.out.println("[CONFIG] Loaded crawler.json successfully");
            System.out.println("[CONFIG] Seed URLs  : " + config.getSeedUrls());
            System.out.println("[CONFIG] Max Depth  : " + config.getMaxDepth());
            System.out.println("[CONFIG] Parallelism: " + config.getParallelism());

            return config;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load crawler.json", e);
        }
    }
}