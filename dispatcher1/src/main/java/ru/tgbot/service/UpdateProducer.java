package ru.tgbot.service;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public interface UpdateProducer {

    void produce(String rabbitQueue, Update update);
}
