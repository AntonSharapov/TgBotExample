package ru.tgbot.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.tgbot.dao.AppDocumentDAO;
import ru.tgbot.dao.BinaryContentDAO;
import ru.tgbot.entity.AppDocument;
import ru.tgbot.entity.BinaryContent;
import ru.tgbot.exceptions.UploadFileException;
import ru.tgbot.service.FileService;

@Log4j

@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    private final AppDocumentDAO appDocumentDao;
    private final BinaryContentDAO binaryContentDao;

    public FileServiceImpl(AppDocumentDAO appDocumentDao, BinaryContentDAO binaryContentDao){
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
    }
    @Override
    public AppDocument processDoc(Message externalMessage) {
        String fileId;
        fileId = externalMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
            log.info("cоздан путь");
            byte[] fileInByte = downloadFile(filePath);
            log.info("cкачан путь");
            BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte)
                .build();
            BinaryContent persistentBinaryContent = binaryContentDao.save(transientBinaryContent);
            log.info("сохранен файл в бинарном виде");
            Document telegramDoc = externalMessage.getDocument();
            log.info("получен сам документ");
            AppDocument transientAppDoc = buildTransintAppDoc(telegramDoc, persistentBinaryContent);
            log.info("документ сохранен в Appdocument");
            return appDocumentDao.save(transientAppDoc);
        } else {
            throw new UploadFileException("Bad response from Telegram service: " +
                response);
        }
    }

    private AppDocument buildTransintAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
            .telegramFileId(telegramDoc.getFileId())
            .docName(telegramDoc.getFileName())
            .binaryContent(persistentBinaryContent)
            .mimeType(telegramDoc.getMimeType())
            .fileSize(telegramDoc.getFileSize())
            .build();
    }

    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}", token)
            .replace("{filePath}", filePath);
        URL urlObj = null;
        try {
            urlObj = new URL(fullUri);
        } catch (MalformedURLException e) {
            throw new UploadFileException(e);
        }

        //TODO подумать над оптимизацией скачивания больших файлов
        try(InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        var request = new HttpEntity<>(headers);

        return restTemplate.exchange(
            fileInfoUri,
            HttpMethod.GET,
            request,
            String.class,
            token, fileId
        );
    }

    private String getFilePath(ResponseEntity<String> response) {
        var jsonObject = new JSONObject(response.getBody());
        return String.valueOf(jsonObject
            .getJSONObject("result")
            .getString("file_path"));
    }
}
