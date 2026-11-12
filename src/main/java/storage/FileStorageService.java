package storage;

import db.DataBase;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileStorageService {
    private final Path storageDb = Path.of("data", "database.db");
    private final Path tmpDb = Path.of("data", "database.db.tmp");
    public FileStorageService() throws IOException {
        Path storageDir = Path.of("data");
        Files.createDirectories(storageDir);
    }

    public void save(DataBase db) throws IOException {
        if (db == null) {
            throw new IllegalArgumentException("Database cannot be null");
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(tmpDb)))) {
            oos.writeObject(db);
            oos.flush();
        } catch (IOException e) {
            Files.deleteIfExists(tmpDb);
            throw new RuntimeException("Failed to save database to file", e);
        }
        Files.move(tmpDb, storageDb, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public DataBase load() throws IOException {
        if (check()) {
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(storageDb)))) {
                return (DataBase) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("Failed to load database from file", e);
            }
        }
        else {
            return new DataBase();
        }
    }

    public boolean check() throws IOException {
        return Files.exists(storageDb) && Files.isRegularFile(storageDb) && Files.size(storageDb) > 0;
    }
}
