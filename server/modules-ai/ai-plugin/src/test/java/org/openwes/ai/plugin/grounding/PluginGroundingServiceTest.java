package org.openwes.ai.plugin.grounding;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.common.utils.exception.WmsException;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginGroundingServiceTest {

    private final ObjectProvider<VectorStore> provider = mock(ObjectProvider.class);
    private final VectorStore vectorStore = mock(VectorStore.class);
    private final AiPluginProperties properties = new AiPluginProperties();
    private final PluginGroundingService service = new PluginGroundingService(provider, properties);

    @Test
    void disabled_returnsEmptyWithoutTouchingTheStore() {
        properties.setGroundingEnabled(false);

        assertTrue(service.retrieve("waves").isEmpty());
        verify(provider, never()).getIfAvailable();
    }

    @Test
    void enabledWithoutAStore_returnsEmpty() {
        properties.setGroundingEnabled(true);
        when(provider.getIfAvailable()).thenReturn(null);

        assertTrue(service.retrieve("waves").isEmpty());
    }

    @Test
    void retrievalFailure_degradesToEmpty() {
        properties.setGroundingEnabled(true);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new IllegalStateException("pinecone down"));

        assertTrue(service.retrieve("waves").isEmpty());
    }

    @Test
    void enabled_returnsTheTopKSnippets() {
        properties.setGroundingEnabled(true);
        properties.setGroundingTopK(3);
        when(provider.getIfAvailable()).thenReturn(vectorStore);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("first"), new Document("second")));

        assertEquals(List.of("first", "second"), service.retrieve("waves"));
    }

    @Test
    void refreshCorpus_replacesEveryContractDocument() {
        when(provider.getIfAvailable()).thenReturn(vectorStore);

        int count = service.refreshCorpus();

        assertEquals(12, count); // wes 9 archetypes + station 3 archetypes
        verify(vectorStore).delete(any(Filter.Expression.class));
        verify(vectorStore).add(anyList());
    }

    @Test
    void refreshCorpus_withoutAStore_failsLoudly() {
        when(provider.getIfAvailable()).thenReturn(null);

        assertThrows(WmsException.class, service::refreshCorpus);
    }

    @Test
    void deleteCorpus_withoutAStore_failsLoudly() {
        when(provider.getIfAvailable()).thenReturn(null);

        assertThrows(WmsException.class, service::deleteCorpus);
    }
}
