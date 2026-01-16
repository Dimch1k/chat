package com.example.chat.controller;

import com.example.chat.model.Chat;
import com.example.chat.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class ChatController {

    private ChatService chatService;

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("chats", chatService.getAllChats());
        return "chat";
    }

    @GetMapping("/chat/{chatId}")
    public String showChat(@PathVariable Long chatId, Model model) {
        model.addAttribute("chats", chatService.getAllChats());
        model.addAttribute("chat", chatService.getChat(chatId));
        return "chat";
    }

    @PostMapping("/chat/new")
    public String showChat(@RequestParam String title) {
        Chat chat = chatService.createNewChat(title);
        return "redirect:/chat/" + chat.getId() ;
    }

    @PostMapping("/chat/{chatId}/delete")
    public String showChat(@PathVariable Long chatId) {
        chatService.deleteChat(chatId);
        return "redirect:/";
    }

//    @PostMapping("/chat/{chatId}/entry")
//    public String talkToModel(@PathVariable Long chatId, @RequestParam String prompt) {
//        chatService.proceedInteraction(chatId, prompt);
//        return "redirect:/chat/" + chatId;
//    }
}
