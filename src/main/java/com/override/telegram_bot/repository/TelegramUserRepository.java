package com.override.telegram_bot.repository;

import com.override.telegram_bot.model.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
    TelegramUser findTelegramUserByChatId(Long chatId);

}
