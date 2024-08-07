package com.override.telegram_bot.service;

import com.override.telegram_bot.enums.MessageContants;
import com.override.telegram_bot.properties.OwnerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class TelegramUserService {

    @Autowired
    private OwnerProperties ownerProperties;

    public boolean isOwner(User user) {
        return ownerProperties.getNamesOwnerTelegramBot().contains(user.getUserName());
    }

    public boolean isOwner(Update update) {
        return isOwner(Optional.ofNullable(update)
                .map(Update::getMessage).map(Message::getFrom)
                .orElseGet(() -> update.getCallbackQuery().getFrom()));
    }

    public String getNewServerUserName(String user) {
        Pattern p = Pattern.compile("(([a-zA-Z].*[0-9])|([a-zA-Z].*))");
        if (user != null && user.matches(p.pattern())) {
            return user.toLowerCase().trim();
        }
        throw new IllegalArgumentException(MessageContants.ERROR_USER_NAME);
    }


}
