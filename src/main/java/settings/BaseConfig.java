package settings;

import controllers.MainWindowController;
import entitys.BaseElement;
import entitys.OS;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BaseConfig {
    private static final Logger LOGGER = Logger.getLogger(BaseConfig.class);
    private static List<BaseElement> baseConfigByUser = new ArrayList<>();

    public static void readBase(String userName, OS operatingSystem) {
        LOGGER.info(String.format("Чтение коллекции конфигураций пользователя [%s]", userName));
        List<String> list = new ArrayList<>();
        File file = new File(operatingSystem.basePathConstructor(userName));
        int stringCount = 0;
        StringBuilder string = new StringBuilder();
        BaseElement baseElement = null;
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    string.append(reader.readLine());
                    if (string.toString().charAt(0) == 65279) { //это ZERO WIDTH NO-BREAK SPACE по сути это char -
                        // символ кодировки UTF-8 без BOM (Byte order Mark) знак порядка байтов
                        string.deleteCharAt(0); //удалим его и добавим при записи
                    }
                    if (!string.toString().startsWith("[") || !string.toString().endsWith("]")) {
                        Objects.requireNonNull(baseElement).addConfigParameter(string.toString());
                    } else if (baseElement != null) {
                        baseConfigByUser.add(baseElement);
                    }
                    if (string.toString().startsWith("[") || string.toString().endsWith("]")) {
                        baseElement = new BaseElement(string.deleteCharAt(0).deleteCharAt(string.length() - 1).toString());
                    }
                    string.delete(0, string.length());
                    stringCount++;
                }
                LOGGER.info(String.format("Коллекция конфигураций пользователя [%s] загружена. Прочитано [%s] строк", userName, stringCount));
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    public static List<String> getBaseList() {
        List<String> list = new ArrayList<>();
        for (BaseElement b : baseConfigByUser) {
            list.add(b.getBaseName());
        }

        return list;
    }

    public static void clearBaseList(){
        baseConfigByUser.clear();
    }
}
