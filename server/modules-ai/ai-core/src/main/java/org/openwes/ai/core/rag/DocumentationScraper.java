package org.openwes.ai.core.rag;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.util.*;

@Slf4j
public class DocumentationScraper {

    private final Set<String> visitedUrls = new HashSet<>();
    private final String baseDomain;

    public DocumentationScraper(String baseUrl) {
        this.baseDomain = extractDomain(baseUrl);
    }

    public List<Document> scrape(String startUrl) {
        List<Document> documents = new ArrayList<>();
        scrapeRecursive(startUrl, documents);
        return documents;
    }

    private void scrapeRecursive(String url, List<Document> documents) {
        // Skip if already visited or not same domain
        if (visitedUrls.contains(url) || !isSameDomain(url)) {
            return;
        }

        visitedUrls.add(url);

        try {
            org.jsoup.nodes.Document doc = Jsoup.connect(url).get();

            // Process current page
            String content = doc.select("main").text();
            if (StringUtils.isNotEmpty(content)) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("source_url", url);
                metadata.put("title", doc.title());
                metadata.put("scrape_timestamp", System.currentTimeMillis());

                documents.add(new Document(content, metadata));
            }

            // Find all links and process them recursively
            Elements links = doc.select("a[href]");
            for (Element link : links) {
                String href = link.attr("href");

                // Normalize URL
                String absoluteUrl = normalizeUrl(url, href);

                // Skip if not a documentation link or invalid URL
                if (absoluteUrl == null || !isDocumentationLink(absoluteUrl)) {
                    continue;
                }

                // Recursive call
                scrapeRecursive(absoluteUrl, documents);
            }

        } catch (IOException e) {
            log.error("Error fetching page: {}", url);
        }
    }

    private String normalizeUrl(String baseUrl, String href) {
        try {
            // Handle relative URLs
            if (href.startsWith("/")) {
                return baseDomain + href;
            }

            // Handle full URLs
            if (href.startsWith("http")) {
                return href;
            }

            // Handle relative paths without leading slash
            return baseUrl.substring(0, baseUrl.lastIndexOf('/') + 1) + href;

        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSameDomain(String url) {
        return url.startsWith(baseDomain);
    }

    private String extractDomain(String url) {
        // Remove protocol and path
        String domain = url.replaceFirst("^(https?://)?(www\\.)?", "");
        domain = domain.split("/")[0];
        return "https://" + domain;
    }

    // Your existing filter method
    private boolean isDocumentationLink(String href) {
        // Skip non-documentation URLs based on patterns seen in your logs
        if (href.contains("/assets/") || href.contains("/blog/") || href.contains("#")) {
            return false;
        }

        // Focus on documentation paths
        return href.contains("/docs/") || href.equals(baseDomain) || href.equals(baseDomain + "/");
    }
}
