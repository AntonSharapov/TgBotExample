package ru.tgbot.service.impl;

import static ru.tgbot.entity.enums.UserState.BASIC_STATE;
import static ru.tgbot.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.tgbot.service.enums.ServiceCommands.*;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.tgbot.dao.AppUserDAO;
import ru.tgbot.dao.RawDataDao;
import ru.tgbot.entity.AppDocument;
import ru.tgbot.entity.AppUser;
import ru.tgbot.entity.RawData;
import ru.tgbot.entity.enums.UserState;
import ru.tgbot.exceptions.UploadFileException;
import ru.tgbot.service.FileService;
import ru.tgbot.service.MainService;
import ru.tgbot.service.ProducerService;
import ru.tgbot.service.enums.ServiceCommands;

@AllArgsConstructor
@Service
@Log4j
public class MainServiceIImpl implements MainService {

    private final RawDataDao rawDataDao;
    private final ProducerService producerService;
    private final AppUserDAO appUserDao;
    private final FileService fileService;

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getUserState();
        var text = update.getMessage().getText();
        var output = "";

        ServiceCommands serviceCommands = ServiceCommands.fromValue(text);

        if(CANCEL.equals(serviceCommands)) {
            output = cancellProcess(appUser);

        } else if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);

        } else if (WAIT_FOR_EMAIL_STATE.equals(userState)) {
            //TODO лдобавить обработку email
        } else {
            log.error("Unknown user state: " + userState);
            output = "Неизвестная ошибка. Введите /cancel и попробуйте снова";
        }
        var chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        log.debug("Node: you are in method");
        saveRawData(update);
        log.debug("Node: Doc data has been saved");
        AppUser appUser = findOrSaveAppUser(update);
        log.debug("Node: App user has been added");
        Long chatId = update.getMessage().getChatId();
        if(isNotAllowedToSendContent(chatId, appUser)) {
            log.debug("ошибка при обработке документа");
            String answer = "ошибка при отправке документа";
            sendAnswer(answer, chatId);
        }

        try {

            AppDocument doc = fileService.processDoc(update.getMessage());


            //TODO Добавить генерацию ссылки для скачивания документа
            String answer = "Документ успешно загружен! " +
                "Ссылка для скачивания: http//test.ru/get-doc/777";
            sendAnswer(answer, chatId);
        } catch (UploadFileException ex){
            log.error(ex);
            String error = "К сожалению, загрузка файла не удалась. Повторите попытку позднее.";
            sendAnswer(error, chatId);
        }
    }

    private boolean isNotAllowedToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getUserState();
        if(!appUser.getIsActive()) {
            String error = "Зарегистрируйте или активируйте свою учётную запись для" +
                "загрузки контента";
            sendAnswer(error, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)) {
            String erorr = "Отмените текущую комманду с помощью /cancel для отправки файлов";
            sendAnswer(erorr, chatId);
            return false;
        }
        return false;
    }

    @Override

    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if(isNotAllowedToSendContent(chatId, appUser)) {
            return;
        }
        //TODO добавить сохранение фото;
        String answer = "Фото успешно загружено. " +
            "Ссылка для скачивания: https://test.ru/get-photo/777";
        sendAnswer(answer,chatId);

    }

    private void sendAnswer(String output, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(output);
        producerService.producerAnswer(sendMessage);
    }

    private String processServiceCommand(AppUser appUser, String cmd) {
        ServiceCommands serviceCommands = ServiceCommands.fromValue(cmd);
        if (REGISTRATION.equals(serviceCommands)) {
            //TODO добавить регистрацию
            return "Временно недоступно";
        } else if (HELP.equals(serviceCommands)) {
            return help();
        } else if (START.equals(serviceCommands)) {
            return "Приветствую! Чтобы просмотреть список " +
                "доступных команд введите /help";
        } else {
            return "Неизвестная команда! Введите команду /help";
        }
    }

    private String help() {
        return "Cписок доступных команд: \n"
            + "/cancel - отмена выполнения текущей команды; \n"
            + "/registration - регистрация пользователя;";
    }

    private String cancellProcess(AppUser appUser) {
        appUser.setUserState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена.";
    }

    private AppUser findOrSaveAppUser(Update update){
        log.debug("Node: You are trying to find or save user");
        var telegramUser = update.getMessage().getFrom();
        log.debug("Node: link have benn resieved");

        var persistentAppUser = appUserDao.findByTelegramUserId(telegramUser.getId().longValue());

        log.debug("Node: finded by telegram Id: " + telegramUser.getId());
        if(persistentAppUser.isEmpty()) {
            AppUser transientAppUser = AppUser.builder()
                .telegramUserId(telegramUser.getId())
                .userName(telegramUser.getUserName())
                .firstName(telegramUser.getFirstName())
                .lastName(telegramUser.getLastName())
                .isActive(true)
                .userState(BASIC_STATE)
                .build();
            return appUserDao.save(transientAppUser);
        }
        return persistentAppUser.get();

    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder()
            .event(update)
            .build();

        rawDataDao.save(rawData);
    }

}
