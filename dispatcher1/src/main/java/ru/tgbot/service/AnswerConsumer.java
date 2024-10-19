package ru.tgbot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public interface AnswerConsumer {

    void consume(SendMessage sendMessage);
}
