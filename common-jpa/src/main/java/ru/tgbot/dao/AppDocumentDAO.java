package ru.tgbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tgbot.entity.AppDocument;

@Repository
public interface AppDocumentDAO extends JpaRepository<AppDocument, Long> {
}
