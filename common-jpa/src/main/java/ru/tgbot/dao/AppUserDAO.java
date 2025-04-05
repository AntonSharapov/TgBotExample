package ru.tgbot.dao;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.tgbot.entity.AppUser;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByTelegramUserId(Long id);
}
