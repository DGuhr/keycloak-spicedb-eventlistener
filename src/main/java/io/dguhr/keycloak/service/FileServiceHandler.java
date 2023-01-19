package io.dguhr.keycloak.service;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * mostly for testing and debugging purposes.
 */
public class FileServiceHandler extends ServiceHandler {

    private static final Logger logger = Logger.getLogger(FileServiceHandler.class);

    public FileServiceHandler(KeycloakSession session, Config.Scope config){
        super(session, config);
        validateConfig();
    }

    @Override
    public void handle(String eventId, String eventValue) throws ExecutionException, InterruptedException, TimeoutException {
        if (eventValue.equals("")) {
           logger.info("no processable event, idling...");
           return;
        }

        logger.info("[SpiceDbEventListener] File handler is writing event id: " + eventId + " with value: " + eventValue + " to file: " + getFileName());
        var filePath = System.getProperty("kc.home.dir");

        Path p = Paths.get(filePath+"spicedb_export.txt");
        try {
            Files.write(p, List.of(eventValue + System.lineSeparator()), CREATE, APPEND);
        } catch (IOException e) {
            logger.error("Not possible! nah! Path: " + p, e);
        }
    }

    private String getFileName() {
        return "spicedb_export.txt";
    }

    @Override
    public void validateConfig() {

    }
}
