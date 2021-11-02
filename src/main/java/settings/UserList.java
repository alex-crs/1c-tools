package settings;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static controllers.MainWindowController.SYSTEM_LIST;

public class UserList {
    private final Logger LOGGER = Logger.getLogger(UserList.class);
    private List<String> userList;

    public UserList(String userListSource) {
        switch (userListSource) {
            case "system":
                this.userList = createUserListFromSystemSource();
                break;
            case "local":
                this.userList = createUserListFromFile();
                break;
        }
    }

    //создает список пользователей из файла
    private List<String> createUserListFromFile() {
        List<String> list = new ArrayList<>();
        File file = new File("users.txt");
        LOGGER.info(String.format("Чтение списка пользователей из файла [%s]", file.getName()));
        AtomicInteger stringCount = new AtomicInteger();
        try {
            if (file.exists() && file.length() > 0) {
                list = Files.newBufferedReader(file.toPath()).lines()
                        .peek(i -> stringCount.getAndIncrement())
                        .collect(Collectors.toList());
            } else if (file.length() == 0) {
                LOGGER.info(String.format("Файл [%s] не содержит записей.", file.getName()));
                list = null;
            } else {
                LOGGER.info(String.format("Файл [%s] не обнаружен.", file.getName()));
                list = null;
            }
            LOGGER.info(String.format("Прочитано [%s] строк.", stringCount));
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return list;
    }

    //возвращает список пользователей. Если список не был сформирован возвращает единственное значение
    //"Список не найден"
    public List<String> getUserList() {
        if (userList == null) {
            userList = new ArrayList<>(Collections.singletonList("Пользователи не найдены"));
        }
        return userList;
    }

    //загрузка списка пользователей из системы
    public List<String> createUserListFromSystemSource() {
        List<String> list = new ArrayList<>();
        LOGGER.info("Получение списка системных пользователей");
        try {
            String query = "SELECT * FROM Win32_UserAccount";
            ActiveXComponent axWMI = new ActiveXComponent("winmgmts:\\");
            LOGGER.info("Выполняю системный запрос");
            Variant vCollection = axWMI.invoke("ExecQuery", new Variant(query));
            LOGGER.info(String.format("Запрос выполнен, результат: %s", vCollection.toString())); //на данный момент не протестировано !!
            EnumVariant enumVariant = new EnumVariant(vCollection.toDispatch());
            Dispatch item = null;
            while (enumVariant.hasMoreElements()) {
                item = enumVariant.nextElement().toDispatch();
                list.add(Dispatch.call(item, "Name").toString());
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return list;
    }
}
