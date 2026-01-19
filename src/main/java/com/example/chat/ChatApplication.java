package com.example.chat;

import com.example.chat.repository.ChatRepository;
import com.example.chat.service.PostgressChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatApplication {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private VectorStore vectorStore;

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        getHistoryAdvisor(),
                        SimpleLoggerAdvisor.builder().build(),
                        getRagAdviser())
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.2)
                        .topP(0.7)
                        .topK(20)
                        .repeatPenalty(1.1)
                        .build())
                .build();
    }

    private Advisor getRagAdviser() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(2)
                        .similarityThreshold(0.6)
                        .build())
                .build();
    }

    private Advisor getHistoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).build();
    }

    private PostgressChatMemory getChatMemory() {
        return PostgressChatMemory.builder()
                .maxMessages(6)
                .chatRepository(chatRepository)
                .build();
    }

}
