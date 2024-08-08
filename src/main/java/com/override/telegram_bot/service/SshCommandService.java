package com.override.telegram_bot.service;

import com.override.telegram_bot.enums.MessageContants;
import com.override.telegram_bot.properties.ServerProperties;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.FileSystemFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static net.schmizz.sshj.SSHClient.DEFAULT_PORT;

@Service
public class SshCommandService {

    @Autowired
    private ServerProperties serverProperties;

    @Value("${script.name}")
    private String scriptName;

    public Session authToServer(String serverIP, String pathToPrivateKey, String serverUserName, SSHClient sshConnect) throws UserAuthException {
        try {
            KeyProvider keys = sshConnect.loadKeys(pathToPrivateKey);
            sshConnect.addHostKeyVerifier(new PromiscuousVerifier());
            sshConnect.connect(serverIP, DEFAULT_PORT);
            sshConnect.authPublickey(serverUserName, keys);
            return sshConnect.startSession();
        } catch (IOException ex) {
            throw new UserAuthException(MessageContants.ERROR_AUTH_TO_SERVER);
        }
    }

    public String uploadFileToServer(String serverIP, String userName) throws UserAuthException {
        try (SSHClient sshConnect = new SSHClient()) {
            KeyProvider keys = sshConnect.loadKeys(serverProperties.getPathToPrivateKey());
            sshConnect.addHostKeyVerifier(new PromiscuousVerifier());
            sshConnect.connect(serverIP, DEFAULT_PORT);
            sshConnect.authPublickey(serverProperties.getUser(), keys);
            sshConnect.newSCPFileTransfer().upload(new FileSystemFile("src/main/resources/script/script"), "/home/" + userName + "/");
            return String.format(MessageContants.FILE_UPLOAD, scriptName, userName);
        } catch (IOException ex) {
            throw new UserAuthException(MessageContants.ERROR_AUTH_TO_SERVER);
        }
    }

    public String execCommand(String ip, String command) {
        try (SSHClient sshConnect = new SSHClient();
             Session session = authToServer(ip, serverProperties.getPathToPrivateKey(), serverProperties.getUser(), sshConnect)) {
            Session.Command cmd = session.exec(command);
            String resultCommand = IOUtils.readFully(cmd.getInputStream()).toString();
            System.out.println("========stdout==========\n" + resultCommand);
            return resultCommand.isEmpty() ? "👍" : resultCommand;
        } catch (IOException e) {
            return MessageContants.ERROR_EXEC_COMMAND_TO_REMOTE_SERVER;
        }
    }


    public String execCommand(String command) {
        try (SSHClient sshConnect = new SSHClient();
             Session session = authToServer(serverProperties.getIp(), serverProperties.getPathToPrivateKey(), serverProperties.getUser(), sshConnect)) {
            Session.Command cmd = session.exec(command);
            String resultCommand = IOUtils.readFully(cmd.getInputStream()).toString();
            System.out.println("========stdout==========\n" + resultCommand);
            return resultCommand.isEmpty() ? "👍" : resultCommand;
        } catch (IOException e) {
            return MessageContants.ERROR_EXEC_COMMAND_TO_LOCAL_SERVER;
        }
    }
}
