package com.webcrawler;

import com.webcrawler.config.CrawlerConfig;
import com.webcrawler.crawler.WordCounter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class WordCounterTest {

    private WordCounter wordCounter;

    @Before
    public void setUp() {
        // Create a real CrawlerConfig with test data — no JSON file needed
        CrawlerConfig config = new CrawlerConfig();
        config.setIgnoredWords(Arrays.asList("the", "and", "is", "are"));
        wordCounter = new WordCounter(config);
    }

    @Test
    public void testBasicWordCount() {
        wordCounter.countWords("java is great java is fast java");

        Map<String, Integer> counts = wordCounter.getWordCountsAsMap();

        // "java" appears 3 times
        assertTrue(counts.containsKey("java"));
        assertEquals(3, (int) counts.get("java"));
    }

    @Test
    public void testIgnoredWordsAreExcluded() {
        wordCounter.countWords("java is the best and great");

        Map<String, Integer> counts = wordCounter.getWordCountsAsMap();

        // "is", "the", "and" should be ignored
        assertFalse("'is' should be ignored",  counts.containsKey("is"));
        assertFalse("'the' should be ignored", counts.containsKey("the"));
        assertFalse("'and' should be ignored", counts.containsKey("and"));

        // "java", "best", "great" should be counted
        assertTrue(counts.containsKey("java"));
        assertTrue(counts.containsKey("best"));
        assertTrue(counts.containsKey("great"));
    }

    @Test
    public void testTopNOrdering() {
        wordCounter.countWords("java java java python python ruby");

        List<Map.Entry<String, Integer>> top = wordCounter.getTopN(3);

        // First word should be "java" with count 3
        assertEquals("java",  top.get(0).getKey());
        assertEquals(3,       (int) top.get(0).getValue());
        assertEquals("python", top.get(1).getKey());
        assertEquals(2,        (int) top.get(1).getValue());
    }

    @Test
    public void testEmptyTextDoesNotCrash() {
        wordCounter.countWords("");
        wordCounter.countWords(null);
        wordCounter.countWords("   ");

        // Should complete without exception, counts should be empty
        assertTrue(wordCounter.getWordCountsAsMap().isEmpty());
    }

    @Test
    public void testShortWordsSkipped() {
        wordCounter.countWords("I am in it go do");

        // All words are < 3 chars, all should be skipped
        assertTrue("Short words should be skipped",
                   wordCounter.getWordCountsAsMap().isEmpty());
    }
}