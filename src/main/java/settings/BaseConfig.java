package settings;

import controllers.MainWindowController;
import entitys.OS;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BaseConfig {
    private static final Logger LOGGER = Logger.getLogger(BaseConfig.class);

    public static List<String> readBase(String userName, OS operatingSystem) {
        LOGGER.info(String.format("Чтение коллекции конфигураций пользователя [%s]",userName));
        List<String> list = new ArrayList<>();
        File file = new File(operatingSystem.basePathConstructor(userName));
        int stringCount = 0;
        StringBuilder string = new StringBuilder();
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    string.delete(0, string.length());
                    string.append(reader.readLine());
                    if (string.toString().startsWith("[")) {
                        list.add(string.deleteCharAt(0).deleteCharAt(string.length() - 1).toString());
                    }
                    stringCount++;
                }
                LOGGER.info(String.format("Коллекция конфигураций пользователя [%s] загружена. Прочитано [%s] строк",userName, stringCount));
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
        return list;
    }
}
