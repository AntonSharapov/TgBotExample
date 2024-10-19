package ru.tgbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tgbot.entity.AppDocument;

public interface AppDocumentDao extends JpaRepository<AppDocument, Long> {
}
