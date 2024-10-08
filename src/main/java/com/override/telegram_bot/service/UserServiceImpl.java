package com.override.telegram_bot.service;


import com.override.telegram_bot.enums.BashCommands;
import com.override.telegram_bot.enums.MessageContants;
import com.override.telegram_bot.model.Server;
import com.override.telegram_bot.model.User;
import com.override.telegram_bot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerServiceImpl serverService;

    @Autowired
    private SshCommandService sshCommandService;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUser(Long id) {
        Optional<User> userFromDb = userRepository.findById(id);
        return userFromDb.orElseThrow();
    }

    public void saveUser(User user) {
        Optional<User> optionalUser = userRepository.findByName(user.getName());
        if (optionalUser.isPresent()) {
            return;
        }
        userRepository.save(user);
    }

    public void updateUser(User updatedUser) {
        User newUser = findUser(updatedUser.getId());
        newUser.setName(updatedUser.getName());
        Set<Server> newServers =
                newUser.getServers().stream()
                        .filter(s -> updatedUser.getServers().stream().map(Server::getIp).toList().contains(s.getIp()))
                        .collect(Collectors.toSet());
        newUser.setServers(newServers);
        newUser.getServers().stream()
                .filter(server -> !newServers.contains(server))
                .map(Server::getIp)
                .forEach(ip -> sshCommandService.execCommand(ip, String.format(BashCommands.DELUSER, updatedUser.getName())));
        userRepository.save(newUser);
    }

    public void deleteUser(Long id) {
        User user = findUser(id);
        String username = user.getName();
        user.getServers().stream()
                .map(Server::getIp)
                .forEach(ip -> sshCommandService.execCommand(ip, String.format(BashCommands.DELUSER, username)));
        userRepository.deleteById(id);
    }

    public String createOrUpdateUserServer(String serverIp, String userName) {
        Server serverFromDB = serverService.findServerByIp(serverIp);
        List<User> users = findAllUsers();
        boolean isNotUserContainsServer = users.stream()
                .filter(user -> user.getName().equals(userName))
                .anyMatch(user -> !user.getServers().contains(serverFromDB));
        if (isNotUserContainsServer) {
            User existingUser = users.stream().filter(user -> user.getName().equals(userName)).findFirst().get();
            Set<Server> servers = existingUser.getServers();
            servers.add(serverFromDB);
            existingUser.setServers(servers);
            userRepository.save(existingUser);
            return String.format(MessageContants.USER_UPDATE_IN_DB, userName, serverIp);
        }
        User user = new User();
        user.setName(userName);
        user.setServers(Collections.singleton(serverFromDB));
        saveUser(user);
        return String.format(MessageContants.USER_CREATE_IN_DB, userName);
    }
}
