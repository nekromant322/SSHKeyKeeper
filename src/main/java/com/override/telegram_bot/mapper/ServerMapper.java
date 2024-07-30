package com.override.telegram_bot.mapper;

import com.override.telegram_bot.dto.ServerDTO;
import com.override.telegram_bot.model.Server;

public class ServerMapper {
    public static ServerDTO userToUserDTO(Server server) {
        return new ServerDTO(
                server.getId(),
                server.getName(),
                server.getIp()
        );
    }

    public static Server userDTOToUser(ServerDTO serverDTO) {
        return new Server(
                serverDTO.getId(),
                serverDTO.getName(),
                serverDTO.getIp()
        );

    }
}
