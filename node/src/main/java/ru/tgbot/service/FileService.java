package ru.tgbot.service;

import org.telegram.telegrambots.meta.api.objects.Message;
import ru.tgbot.entity.AppDocument;

public interface FileService {
    AppDocument processDoc(Message externalMessage);
}
