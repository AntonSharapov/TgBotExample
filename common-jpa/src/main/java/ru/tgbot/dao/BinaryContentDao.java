package ru.tgbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tgbot.entity.BinaryContent;

public interface BinaryContentDao extends JpaRepository<BinaryContent, Long> {
}
