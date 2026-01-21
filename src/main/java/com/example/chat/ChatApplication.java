package com.example.chat;

import com.example.chat.advisor.expansion.ExpansionQueryAdvisor;
import com.example.chat.advisor.rag.RagAdvisor;
import com.example.chat.repository.ChatRepository;
import com.example.chat.service.PostgressChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ChatApplication {

    public static final PromptTemplate SYSTEM_PROMPT = PromptTemplate.builder()
            .template("""
                    TODO_PROMPT
                    """)
            .build();

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ChatModel chatModel;

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(10),
                        SimpleLoggerAdvisor.builder().order(20).build(),
                        RagAdvisor.builder(vectorStore).order(30).build(),
                        SimpleLoggerAdvisor.builder().order(40).build())
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.2)
                        .topP(0.7)
                        .topK(20)
                        .repeatPenalty(1.1)
                        .build())
                .defaultSystem(SYSTEM_PROMPT.render())
                .build();
    }

    private Advisor getHistoryAdvisor(int order) {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(order).build();
    }

    private PostgressChatMemory getChatMemory() {
        return PostgressChatMemory.builder()
                .maxMessages(6)
                .chatRepository(chatRepository)
                .build();
    }

}
