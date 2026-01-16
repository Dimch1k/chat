package com.example.chat.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Role {

    USER("user") {
        @Override
        Message getMessage(String prompt) {
            return new UserMessage(prompt);
        }
    }, ASSISTANT("assistant") {
        @Override
        Message getMessage(String prompt) {
            return new AssistantMessage(prompt);
        }
    }, SYSTEM("system") {
        @Override
        Message getMessage(String prompt) {
            return new SystemMessage(prompt);
        }
    };

    private final String role;

    public static Role getRole(String role) {
        return Arrays.stream(Role.values()).filter(r -> r.role.equals(role)).findFirst().orElseThrow();
    }

    abstract Message getMessage(String prompt);
}
