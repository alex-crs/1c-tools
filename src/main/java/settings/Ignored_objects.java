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
    private List<String> excludedFiles;
    private final File file = new File("ignore.txt");

    public Ignored_objects() {
        readExcludeList();
    }

    public List<String> getExcludedFilesList() {
        return excludedFiles;
    }

    //чтение списка исключений из файла
    private void readExcludeList() {
        excludedFiles = new ArrayList<>();
        LOGGER.info(String.format("Чтение списка исключений из файла [%s]", file.getName()));
        int stringCount = 0;
        if (file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                while (reader.ready()) {
                    excludedFiles.add(reader.readLine());
                    stringCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (file.length() == 0) {
            LOGGER.info(String.format("Файл [%s] не содержит записей.", file.getName()));
            excludedFiles = fillIgnoredList_default();
            saveExcludeList();
            LOGGER.info("Создан новый файл ignore.txt и заполнен значениями по умолчанию");
        }
        LOGGER.info(String.format("Прочитано [%s] строк.", stringCount));
    }

    //обновление файла исключений
    public void saveExcludeList() {
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            for (String u : excludedFiles) {
                writer.append(u);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> fillIgnoredList_default() {
        return new ArrayList<>(Arrays.asList("logs", "conf", "1cv8u.pfl"));
    }
}
