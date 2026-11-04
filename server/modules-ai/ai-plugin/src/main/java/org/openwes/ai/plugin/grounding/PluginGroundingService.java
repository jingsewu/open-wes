package org.openwes.ai.plugin.grounding;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.exception.AiPluginErrorDescEnum;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optional retrieval grounding over the frozen plugin contract. A missing or broken VectorStore
 * must never block drafting; only an explicit corpus refresh fails, because that is an operator
 * action that cannot silently do nothing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PluginGroundingService {

    static final String CORPUS_SOURCE = "script-plugin-contract";

    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final AiPluginProperties properties;

    public List<String> retrieve(String query) {
        if (!properties.isGroundingEnabled()) {
            return List.of();
        }
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            log.warn("AI grounding is enabled but no VectorStore is configured; continuing without it");
            return List.of();
        }
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(properties.getGroundingTopK())
                    .similarityThresholdAll()
                    .build();
            return store.similaritySearch(request).stream().map(Document::getText).toList();
        } catch (RuntimeException e) {
            log.warn("AI grounding retrieval failed; continuing without it: {}", e.getMessage());
            return List.of();
        }
    }

    public int refreshCorpus() {
        VectorStore store = requireStore();
        store.delete(corpusFilter());
        List<Document> documents = corpusDocuments();
        store.add(documents);
        log.info("AI plugin contract corpus refreshed with {} documents", documents.size());
        return documents.size();
    }

    public void deleteCorpus() {
        requireStore().delete(corpusFilter());
    }

    List<Document> corpusDocuments() {
        List<Document> documents = new ArrayList<>();
        for (String app : ScriptPluginApiConstants.SUPPORTED_APPS) {
            for (ExtensionType type : PluginContractCatalog.typesForApp(app)) {
                String id = CORPUS_SOURCE + ":" + app + ":" + type.name();
                documents.add(new Document(id,
                        PluginContractCatalog.toPromptFragment(app, List.of(type)),
                        Map.of("source", CORPUS_SOURCE, "app", app, "type", type.name())));
            }
        }
        return documents;
    }

    private Filter.Expression corpusFilter() {
        return new FilterExpressionBuilder().eq("source", CORPUS_SOURCE).build();
    }

    private VectorStore requireStore() {
        VectorStore store = vectorStoreProvider.getIfAvailable();
        if (store == null) {
            throw new WmsException(AiPluginErrorDescEnum.AI_CORPUS_UNAVAILABLE);
        }
        return store;
    }
}
