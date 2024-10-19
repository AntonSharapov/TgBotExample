package ru.tgbot.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.tgbot.service.RabbitQueue;
import ru.tgbot.controller.UpdateController;
import ru.tgbot.service.AnswerConsumer;

@Service
@AllArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @RabbitListener(queues = RabbitQueue.ANSWER_MESSAGE)
    @Override
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);

    }
}
