package settings;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static controllers.MainWindowController.SYSTEM_LIST;

public class UserList {
    private final Logger LOGGER = Logger.getLogger(UserList.class);
    private List<String> userList;
    private String panelMode;
    File file = new File("users.txt");

    public UserList(String userListSource) {
        switch (userListSource) {
            case "system":
                this.userList = createUserListFromSystemSource();
                this.panelMode = "system";
                break;
            case "local":
                this.userList = createUserListFromFile();
                this.panelMode = "local";
                break;
        }
    }

    //создает список пользователей из файла
    private List<String> createUserListFromFile() {
        List<String> list = new ArrayList<>();
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

    public void saveUserList() {
        userList = userList.stream().sorted(Comparator.comparing(String::format)).collect(Collectors.toList());
        if (!panelMode.equals("system")) {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                for (String u : userList) {
                    writer.append(u + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addUserToLocalList(List<String> users) {
        userList = userList.stream().filter(s -> !s.equals("Пользователи не найдены")).collect(Collectors.toList());
        users.forEach(s -> {
            if (!userList.contains(s)) {
                userList.add(s);
            }
        });
    }

    public void deleteFromLocalList(List<String> users) {
        users.forEach(s -> userList.remove(s));
        if (userList.size() == 0) {
            userList.add("Пользователи не найдены");
        }
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
