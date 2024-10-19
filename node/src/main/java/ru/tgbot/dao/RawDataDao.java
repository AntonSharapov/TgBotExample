package ru.tgbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tgbot.entity.RawData;

public interface RawDataDao extends JpaRepository<RawData, Long> {
}
