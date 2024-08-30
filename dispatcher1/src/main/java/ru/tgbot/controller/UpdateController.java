package ru.tgbot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tgbot.service.UpdateProducer;
import ru.tgbot.utils.MessageUtils;
import ru.tgbot.RabbitQueue;

@Component
@Slf4j
public class UpdateController {

    private TelegramBot telegramBot;
    private MessageUtils messageUtils;
    private UpdateProducer updateProducer;


    public UpdateController(MessageUtils messageUtils, UpdateProducer updateProducer) {
        this.messageUtils = messageUtils;
        this.updateProducer = updateProducer;
    }

    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }
    
    public void processUpdate(Update update){
        if (update == null) {
            log.error("Received update is null");
        }
        
        if (update.getMessage() != null) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message type");
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.getText() != null) {
            processTextMessage(update);
        } else if (message.getDocument() != null) {
            processDocMessage(update);
        } else if (message.getPhoto() != null) {
            processPhotoMessage(update);
        } else {
            setUnsupportedMessage(update);
        }
    }

    private void setUnsupportedMessage(Update update) {
        var message = messageUtils.generateSendMessageWithText(update,
            "Неподдерживаемый тип сообщения");

        setView(message);
    }
    private void setFileReceivedView(Update update) {
        var message = messageUtils.generateSendMessageWithText(update,
            "Файл получен! Происходит обработка...");

        setView(message);
    }

    private void setView(SendMessage message) {
        telegramBot.sendAnswerMessage(message);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(RabbitQueue.PHOTO_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(RabbitQueue.DOC_MESSAGE_UPDATE, update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(RabbitQueue.TEXT_MESSAGE_UPDATE, update);
    }
}
