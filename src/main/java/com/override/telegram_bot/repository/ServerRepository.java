package com.override.telegram_bot.repository;

import com.override.telegram_bot.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    Optional<Server> findByName(String name);
    Optional<Server> findServerByIp(String ip);
}
