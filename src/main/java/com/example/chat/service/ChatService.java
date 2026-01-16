package com.example.chat.service;

import com.example.chat.model.Chat;
import com.example.chat.model.ChatEntry;
import com.example.chat.model.Role;
import com.example.chat.repository.ChatRepository;
import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static com.example.chat.model.Role.ASSISTANT;
import static com.example.chat.model.Role.USER;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ChatClient chatClient;

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    public Chat getChat(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow();
    }

    public Chat createNewChat(String chatName) {
        Chat chat = Chat.builder().title(chatName).build();
        return chatRepository.save(chat);
    }

    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    @Transactional
    public void proceedInteraction(Long chatId, String prompt) {
        addChatEntry(chatId, prompt, USER);
        String answer = chatClient.prompt().user(prompt).call().content();
        addChatEntry(chatId, answer, ASSISTANT);
    }

    private void addChatEntry(Long chatId, String prompt, Role role) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        chat.addEntry(ChatEntry.builder().content(prompt).role(role).build());
        chatRepository.save(chat);
    }

    public SseEmitter proceedInteractionWithStreaming(Long chatId, String prompt) {
        StringBuilder answer = new StringBuilder();
        SseEmitter emitter = new SseEmitter(0L);
        chatClient
                .prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .chatResponse()
                .subscribe(response -> processToken(response, emitter, answer),
                        emitter::completeWithError,
                        emitter::complete);
        return emitter;
    }

    @SneakyThrows
    private static void processToken(ChatResponse response, SseEmitter emitter, StringBuilder answer) {
        AssistantMessage token = response.getResult().getOutput();
        emitter.send(token);
        answer.append(token.getText());
    }
}
