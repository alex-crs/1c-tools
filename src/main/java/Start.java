
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import org.apache.log4j.Logger;

public class Start {
    private static final Logger LOGGER = Logger.getLogger(Start.class);
    private static List<String> excludedFiles;
    private static List<String> userList;

    public static void main(String[] args) {
        excludedFiles = readExcludeList();
        userList = readUserList();
        clearCashByUser(userList);
    }

    //очистка кэша для списка пользователей
    private static void clearCashByUser(List<String> users) {
        if (users != null) {
            LOGGER.info("Происходит удаление кэша 1С согласно списка пользователей");
            for (String u : userList) {
                LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] до очистки [%s]", u,
                        spaceToString(cacheSpaceCalc("c:\\Users" + File.separator + u
                                + File.separator + "AppData\\Local\\1C\\"))));
                clear1sCache("c:\\Users" + File.separator + u + File.separator + "AppData\\Local", "1C");
                LOGGER.info(String.format("Кэш 1С для пользователя [%s] очищен", u));
                LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] после очистки [%s]", u,
                        spaceToString(cacheSpaceCalc("c:\\Users" + File.separator + u
                                + File.separator + "AppData\\Local\\1C\\"))));
            }
        } else {
            String currentUserPath = System.getenv("localappdata");
            LOGGER.info("Удаление кэша 1С для текущего пользователя");
            LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] до очистки [%s]",
                    System.getenv("USERNAME"),
                    spaceToString(cacheSpaceCalc(currentUserPath + "\\1C\\"))));
            clear1sCache(currentUserPath, "1C");
            LOGGER.info(String.format("Размер папки с кэшем для пользователя [%s] после очистки [%s]",
                    System.getenv("USERNAME"),
                    spaceToString(cacheSpaceCalc(currentUserPath + "\\1C\\"))));
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
                    if (!excludedFiles.contains(e) && e.length() > 20) {
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
        Files.walkFileTree(Path.of(path + File.separator + fileName), new SimpleFileVisitor<Path>() {
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

    //загрузка списка пользователей из системы
    private static List<String> createUserList() {
        LOGGER.info("Получение списка системных пользователей");
        List<String> list = new ArrayList<>();
        String query = "SELECT * FROM Win32_UserAccount";
        ActiveXComponent axWMI = new ActiveXComponent("winmgmts:\\");
        Variant vCollection = axWMI.invoke("ExecQuery", new Variant(query));
        EnumVariant enumVariant = new EnumVariant(vCollection.toDispatch());
        Dispatch item = null;
        while (enumVariant.hasMoreElements()) {
            item = enumVariant.nextElement().toDispatch();
            list.add(Dispatch.call(item, "Name").toString());
        }
        return list;
    }

    //чтение списка исключений из файла
    private static List<String> readExcludeList() {
        List<String> list = new ArrayList<>();
        File file = new File("ignore.txt");
        LOGGER.info(String.format("Чтение списка исключений из файла [%s]", file.getName()));
        int stringCount = 0;
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                while (reader.ready()) {
                    list.add(reader.readLine());
                    stringCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (file.length() == 0) {
            LOGGER.info(String.format("Файл [%s] не содержит записей.", file.getName()));
            list = null;
        } else {
            LOGGER.info(String.format("Файл [%s] не обнаружен.", file.getName()));
            list = null;
        }
        LOGGER.info(String.format("Прочитано [%s] строк.", stringCount));
        return list;
    }

    //обновление файла исключений
    private static void saveExcludeList(List<String> excludeList) {
        File file = new File("ignore2.txt"); //в предрелизной версии поправить
        int stringCount = 0;
        try (FileOutputStream fis = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fis))) {
            if (!file.exists()) {
                file.createNewFile();
            }
            for (String e : excludeList) {
                writer.write(e + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //чтение списка пользователей из файла
    private static List<String> readUserList() {
        List<String> list = new ArrayList<>();
        File file = new File("users.txt");
        LOGGER.info(String.format("Чтение списка пользователей из файла [%s]", file.getName()));
        int stringCount = 0;
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                while (reader.ready()) {
                    list.add(reader.readLine());
                    stringCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (file.length() == 0) {
            LOGGER.info(String.format("Файл [%s] не содержит записей.", file.getName()));
            list = null;
        } else {
            LOGGER.info(String.format("Файл [%s] не обнаружен.", file.getName()));
            list = null;
        }
        LOGGER.info(String.format("Прочитано [%s] строк.", stringCount));
        return list;
    }

    //определение размера папки с кэшем
    public static long cacheSpaceCalc(String sourcePath) {
        long totalSize = 0;
        File file;
        File sourceDirectory = new File(sourcePath);
        if (!sourceDirectory.isFile()) {
            String[] sourceDirectoryFilesList = sourceDirectory.list();
            if (sourceDirectoryFilesList != null && sourceDirectoryFilesList.length != 0) {
                for (String element : sourceDirectoryFilesList) {
                    String path = sourcePath + element;
                    file = new File(path);
                    if (file.isFile()) {
                        totalSize += file.length();
                    }
                    if (file.isDirectory()) {
                        totalSize += cacheSpaceCalc(path + File.separator);
                    }
                }
            }
        } else {
            totalSize += sourceDirectory.length();
        }
        return totalSize;
    }

    //форматирование выводимого размера папки с кэшем
    private static String spaceToString(float digit) {
        if (digit < 1000) {
            return String.format("%.0f byte", digit);
        } else if (digit < 1000000) {
            return String.format("%.0f kb", digit / 1000);
        } else if (digit < 1000000000) {
            return String.format("%.2f mb", digit / 1000000);
        } else if (digit < 1000000000000L) {
            return String.format("%.2f Gb", digit / 1000000000L);
        }
        return digit + "bytes";
    }
}
