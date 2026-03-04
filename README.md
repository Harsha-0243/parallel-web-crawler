# Parallel Web Crawler — Java

A production-quality parallel web crawler built in Java demonstrating 
advanced concurrency, clean architecture, and dependency injection.

## Technologies Used
- **Java 17** — Core language
- **Maven** — Build and dependency management
- **ExecutorService** — Thread pool for parallel crawling
- **JSoup** — HTML parsing and link extraction
- **Google Guice** — Dependency Injection
- **Gson** — JSON serialization of crawl results
- **JUnit 4** — Unit testing

## Features
- Crawls multiple seed URLs in parallel using configurable thread pool
- Configurable crawl depth, parallelism, timeouts via `crawler.json`
- Thread-safe URL deduplication using `ConcurrentHashMap`
- Thread-safe word counting using `AtomicInteger`
- Domain whitelist to restrict crawl scope
- Top-N most frequent word extraction
- JSON output of all crawl results
- Graceful error handling — never crashes on bad URLs

## Project Structure
```
src/
├── main/
│   ├── java/com/webcrawler/
│   │   ├── Main.java                  # Entry point
│   │   ├── config/CrawlerConfig.java  # Configuration model
│   │   ├── crawler/
│   │   │   ├── WebCrawler.java        # Main orchestrator
│   │   │   ├── PageFetcher.java       # HTTP fetcher
│   │   │   └── WordCounter.java       # Word frequency counter
│   │   ├── model/CrawlResult.java     # Output data model
│   │   └── di/CrawlerModule.java      # Guice DI module
│   └── resources/
│       └── crawler.json               # Runtime configuration
└── test/
    └── java/com/webcrawler/
        └── WordCounterTest.java       # Unit tests
```

## How to Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Setup
```bash
git clone https://github.com/YOUR_USERNAME/parallel-web-crawler.git
cd parallel-web-crawler
mvn clean compile
mvn exec:java -Dexec.mainClass=com.webcrawler.Main
```

### Configuration
Edit `src/main/resources/crawler.json`:
```json
{
  "seedUrls": ["https://en.wikipedia.org/wiki/Java_(programming_language)"],
  "maxDepth": 1,
  "parallelism": 4,
  "timeoutMs": 5000,
  "topN": 15,
  "resultPath": "output/crawl_result.json"
}
```

## Performance
| Mode | Threads | Pages | Time |
|---|---|---|---|
| Sequential | 1 | 50 | ~40s |
| Parallel | 4 | 50 | ~12s |

**3-4x speedup** from parallelism alone.

## Architecture
```
Main.java
  └── Guice Injector (CrawlerModule)
        └── WebCrawler (ExecutorService thread pool)
              ├── PageFetcher (JSoup HTTP)
              └── WordCounter (ConcurrentHashMap + AtomicInteger)
```

## Key Concepts Demonstrated
- `ExecutorService` with fixed thread pool
- `ConcurrentHashMap.newKeySet()` for thread-safe visited tracking  
- `AtomicInteger` for lock-free word counting
- `Optional<T>` for safe null handling
- Dependency Injection via Google Guice
- Configuration-driven design (no hardcoded values)
```