package handlers;

import org.apache.log4j.Logger;
import settings.Ignored_objects;
import settings.UserList;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static handlers.FileLengthCalculator.*;

public class CacheCleaner {
    private static final Logger LOGGER = Logger.getLogger(CacheCleaner.class);
    private static Ignored_objects ignoredObjects;

    //очистка кэша для списка пользователей
    public static void clearCacheByUser(List<String> users, Ignored_objects ignoreFiles) {
        ignoredObjects = ignoreFiles;
        if (users != null) {
            LOGGER.info("Происходит удаление кэша 1С согласно списка пользователей");
            for (String u : users) {
                LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] до очистки [%s]", u,
                        getOccupiedSpace("c:\\Users" + File.separator + u
                                + File.separator + "AppData\\Local\\1C\\")));
                clear1sCache("c:\\Users" + File.separator + u + File.separator + "AppData\\Local", "1C");
                LOGGER.info(String.format("Кэш 1С для пользователя [%s] очищен", u));
                LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] после очистки [%s]", u,
                        getOccupiedSpace("c:\\Users" + File.separator + u
                                + File.separator + "AppData\\Local\\1C\\")));
            }
        } else {
            String currentUserPath = System.getenv("localappdata");
            LOGGER.info("Удаление кэша 1С для текущего пользователя");
            LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] до очистки [%s]",
                    System.getenv("USERNAME"),
                    getOccupiedSpace(currentUserPath + "\\1C\\")));
            clear1sCache(currentUserPath, "1C");
            LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] после очистки [%s]",
                    System.getenv("USERNAME"),
                    getOccupiedSpace(currentUserPath + "\\1C\\")));
        }
    }

    //очистка кэша из переданного пути
    private static void clear1sCache(String path, String file) {
        File fileOrFolder = new File(path + File.separator + file);
        String[] fileList = fileOrFolder.list();
        try {
            if (fileList != null) {
                for (String e : fileList) {
                    File includedFile = new File(fileOrFolder.getPath() + File.separator + e);
                    if (!ignoredObjects.getExcludedFilesList().contains(e) && e.length() > 20) {
                        deleteItems(fileOrFolder.getPath(), e);
                    } else if (!includedFile.isFile() && includedFile.list().length > 0) {
                        clear1sCache(fileOrFolder.getPath(), e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    //удаление файлов и папок
    private static void deleteItems(String path, String fileName) throws IOException {
        LOGGER.info(String.format("Производится удаление папки [%s] в директории [%s]", fileName, path));
        Files.walkFileTree(Paths.get(path + File.separator + fileName), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
