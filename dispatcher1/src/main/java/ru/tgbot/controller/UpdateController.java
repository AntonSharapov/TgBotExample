package ru.tgbot.controller;

import static ru.tgbot.service.RabbitQueue.DOC_MESSAGE_UPDATE;
import static ru.tgbot.service.RabbitQueue.PHOTO_MESSAGE_UPDATE;
import static ru.tgbot.service.RabbitQueue.TEXT_MESSAGE_UPDATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tgbot.service.UpdateProducer;
import ru.tgbot.utils.MessageUtils;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UpdateController {

    private TelegramBot telegramBot;
    private final MessageUtils messageUtils;
    private final UpdateProducer updateProducer;



    public void registerBot(TelegramBot telegramBot){
        this.telegramBot = telegramBot;
    }
    
    public void processUpdate(Update update){
        if (update == null) {
            log.error("Received update is null");
        }
        
        if (update.hasMessage()) {
            distributeMessageByType(update);
        } else {
            log.error("Received unsupported message type");
        }
    }

    private void distributeMessageByType(Update update) {
        var message = update.getMessage();
        if (message.hasText()) {
            processTextMessage(update);
        } else if (message.hasDocument()) {
            processDocMessage(update);
        } else if (message.hasPhoto()) {
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

    public void setView(SendMessage message) {
        telegramBot.sendAnswerMessage(message);
    }

    private void processPhotoMessage(Update update) {
        updateProducer.produce(PHOTO_MESSAGE_UPDATE, update);
        setFileReceivedView(update);
    }

    private void processDocMessage(Update update) {
        updateProducer.produce(DOC_MESSAGE_UPDATE, update);
    }

    private void processTextMessage(Update update) {
        updateProducer.produce(TEXT_MESSAGE_UPDATE, update);
    }
}
