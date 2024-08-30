package ru.tgbot.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tgbot.RabbitQueue;
import ru.tgbot.service.ConsumerService;
import ru.tgbot.service.ProducerService;

@Service
@Log4j
@AllArgsConstructor
public class ConsumerServiceImpl implements ConsumerService {
    private final ProducerService producerService;

    @RabbitListener(queues = RabbitQueue.TEXT_MESSAGE_UPDATE)
    @Override
    public void consumeTextMessageUpdates(Update update) {
        log.debug("Node: Text Message is recieved");
    }

    @RabbitListener(queues = RabbitQueue.DOC_MESSAGE_UPDATE)
    @Override
    public void consumeDocMessageUpdates(Update update) {
        log.debug("Node: Doc Message is recieved");

    }

    @RabbitListener(queues = RabbitQueue.PHOTO_MESSAGE_UPDATE)
    @Override
    public void consumePhotoMessageUpdates(Update update) {
        log.debug("Node: Photo Message is recieved");
    }
}
