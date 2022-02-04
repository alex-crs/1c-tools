package settings;

import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Ignored_objects {
    private final Logger LOGGER = Logger.getLogger(Ignored_objects.class);
    private final List<String> excludedFiles;
    private final File file = new File("ignore.txt");

    public Ignored_objects() {
        excludedFiles = readExcludeList();
    }

    public List<String> getExcludedFilesList() {
        return excludedFiles;
    }

    //чтение списка исключений из файла
    private List<String> readExcludeList() {
        List<String> list = new ArrayList<>();
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
            list = fillIgnoredList_default();
        } else {
            LOGGER.info(String.format("Файл [%s] не обнаружен.", file.getName()));
            list = null;
        }
        LOGGER.info(String.format("Прочитано [%s] строк.", stringCount));
        return list;
    }

    //обновление файла исключений
    public void saveExcludeList() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (String u : excludedFiles) {
                writer.append(u + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> fillIgnoredList_default() {
        return new ArrayList<>(Arrays.asList("logs", "conf", "1cv8u.pfl"));
    }
}
