package org.openwes.ai.core.domain;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LimitSizeInMemoryChatMemory implements ChatMemory {

    private final int maxSize;

    Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    public LimitSizeInMemoryChatMemory(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be greater than 0");
        }
        this.maxSize = maxSize;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        conversationHistory.putIfAbsent(conversationId, new ArrayList<>());

        if (conversationHistory.get(conversationId).size() > maxSize) {
            conversationHistory.get(conversationId).remove(0);
        }
        conversationHistory.get(conversationId).addAll(messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> all = this.conversationHistory.get(conversationId);
        return all != null ? all.stream().skip(Math.max(0, all.size() - lastN)).toList() : List.of();
    }

    @Override
    public void clear(String conversationId) {
        this.conversationHistory.remove(conversationId);
    }

}
