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
import ru.tgbot.dao.AppDocumentDao;
import ru.tgbot.dao.BinaryContentDao;
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
    private final AppDocumentDao appDocumentDao;
    private final BinaryContentDao binaryContentDao;

    public FileServiceImpl(AppDocumentDao appDocumentDao, BinaryContentDao binaryContentDao){
        this.appDocumentDao = appDocumentDao;
        this.binaryContentDao = binaryContentDao;
    }
    @Override
    public AppDocument processDoc(Message externalMessage) {
        String fieldId = externalMessage.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fieldId);
        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(fileInByte).build();
            BinaryContent persistentBinaryContent = binaryContentDao.save(transientBinaryContent);
            Document telegramDoc = externalMessage.getDocument();
            AppDocument transientAppDoc = buildTransintAppDoc(telegramDoc, persistentBinaryContent);
            return appDocumentDao.save(transientAppDoc);
        } else
            throw new UploadFileException("Bad response from Telegram service: " +
                response);
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

        //TODO подумать над оптимизацией
        try(InputStream is = urlObj.openStream()) {
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(), e);
        }
    }

    private ResponseEntity<String> getFilePath(String fieldId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        return restTemplate.exchange(
            fileInfoUri,
            HttpMethod.GET,
            request,
            String.class,
            token,
            fieldId);
    }
}
