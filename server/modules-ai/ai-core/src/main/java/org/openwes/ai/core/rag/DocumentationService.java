package org.openwes.ai.core.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentationService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter;

    public DocumentationService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.textSplitter = new TokenTextSplitter(
                2000,    // Slightly smaller chunks for technical content
                300,    // Longer minimum size for complete concepts
                100,    //  sDon't embed verymall fragments
                15,     // Limit chunks per page
                true    // Keep separators to maintain structure
        );
    }

    public void scrapeAndStoreDocumentation(String baseUrl) {
        List<Document> documents = new  DocumentationScraper(baseUrl).scrape(baseUrl)
                .stream().flatMap(page -> processDocument(page.getText(), page.getMetadata()).stream())
                .toList();

        vectorStore.add(documents);
    }

    public List<Document> processDocument(String content, Map<String, Object> metadata) {
        Document originalDoc = new Document(content, metadata);
        List<Document> chunks = textSplitter.split(originalDoc);

        // Add chunk-specific metadata
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("chunk_number", i);
            chunks.get(i).getMetadata().put("total_chunks", chunks.size());
        }

        return chunks;
    }
}
