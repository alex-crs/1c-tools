package settings;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Ignored_objects {
    private final Logger LOGGER = Logger.getLogger(Ignored_objects.class);
    private List<String> excludedFiles;

    public Ignored_objects() {
        excludedFiles = readExcludeList();
    }

    public List<String> getExcludedFilesList(){
        return excludedFiles;
    }

    //чтение списка исключений из файла
    private List<String> readExcludeList() {
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
    public void saveExcludeList(List<String> excludeList) {
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
}
