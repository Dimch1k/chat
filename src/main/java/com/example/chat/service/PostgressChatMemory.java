package com.example.chat.service;

import com.example.chat.model.Chat;
import com.example.chat.model.ChatEntry;
import com.example.chat.repository.ChatRepository;
import lombok.Builder;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.Comparator;
import java.util.List;

@Builder
public class PostgressChatMemory implements ChatMemory {

    private ChatRepository chatRepository;
    private int maxMessages;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        for (Message message : messages) {
            chat.addEntry(ChatEntry.toEntry(message));
        }
        chatRepository.save(chat);
    }

    @Override
    public List<Message> get(String conversationId) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        int messagesToSkip = Math.max(0, chat.getHistory().size() - maxMessages);
        return chat.getHistory().stream()
                .skip(messagesToSkip)
                .map(ChatEntry::toMessage)
                .limit(maxMessages)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        // no needed
    }
}
