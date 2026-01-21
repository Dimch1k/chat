package com.example.chat.advisor.expansion;

import lombok.Builder;
import lombok.Getter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import java.util.Map;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    private ChatClient chatClient;

    @Getter
    private final int order;

    private static final PromptTemplate template = PromptTemplate.builder()
            .template("""
                    TODO_PROMPT

                    Question: {question}
                    Reformulated:
                    """)
            .build();

    public static final String ORIGINAL_QUESTION = "ORIGINAL_QUESTION";
    public static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";
    public static final String EXPANSION_RATIO = "EXPANSION_RATIO";

    public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryAdvisorBuilder().chatClient(ChatClient.builder(chatModel)
                .defaultOptions(OllamaChatOptions.builder()
                        .temperature(0.0)
                        .topK(1)
                        .topP(0.1)
                        .repeatPenalty(1.0)
                        .build())
                .build());
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String enrichedQuestion = chatClient
                .prompt()
                .user(template.render(Map.of("question", userQuestion)))
                .call()
                .content();

        double ratio = (double) enrichedQuestion.length() / userQuestion.length();

        return chatClientRequest.mutate()
                .context(ORIGINAL_QUESTION, userQuestion)
                .context(ENRICHED_QUESTION, enrichedQuestion)
                .context(EXPANSION_RATIO, ratio)
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

}
