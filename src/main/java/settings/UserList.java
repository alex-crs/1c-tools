package settings;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;
import controllers.MainWindowController;
import entities.User;
import javafx.scene.control.ComboBox;
import org.apache.log4j.Logger;
import service.DataBaseService;
import stages.AlertWindowStage;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static entities.Const.*;

public class UserList {
    private final Logger LOGGER = Logger.getLogger(UserList.class);
    private List<User> userList;
    private String panelMode;
    DataBaseService data_base;
    ComboBox<String> group_choice_box;
    String currentGroup;
    File file = new File("users.txt");

    public String getCurrentGroup() {
        return currentGroup;
    }

    public UserList(DataBaseService bdService, String userListSource, ComboBox<String> group_choice_box) {
        this.data_base = bdService;
        this.group_choice_box = group_choice_box;
        userListInit(userListSource);
        loadGroupMenu();
    }

    //инициализируем элементы управления и список групп из базы данных
    public void loadGroupMenu() {
        group_choice_box.getItems().clear();
        group_choice_box.getItems().add(CREATE_GROUP.getTitle());
        group_choice_box.getItems().add(RENAME_GROUP.getTitle());
        group_choice_box.getItems().add(DELETE_GROUP.getTitle());
        group_choice_box.getItems().add(GROUPS_DELIMITER.getTitle());
        group_choice_box.getItems().addAll(data_base.getGroups());
//        group_choice_box.setValue(DEFAULT_GROUP.getTitle());
    }

    public void updateUserList(String groupName) {
        loadGroupMenu();
        loadUserListByGroup(groupName);
    }

    public void userListInit(String userListSource) {
        switch (userListSource) {
            case "system":
                this.userList = getUserListFromSystem();
                this.panelMode = "system";
                break;
            case "local":
//                this.userList = getUserListFromFile();
                this.panelMode = "local";
                break;
            case "BD":
                loadUserListByGroup(DEFAULT_GROUP.getTitle());
                this.panelMode = "bd";
                this.currentGroup = DEFAULT_GROUP.getTitle();
                break;
        }
    }

    //позволяет загрузить лист пользователей по имени группы
    //- если группа удалена, то произойдет попытка загрузки последней используемой группы
    //- если текущая группа удалена, то показывается группа по умолчанию
    public void loadUserListByGroup(String userList) {
        if (data_base.doesTheGroupExist(userList) > 0) {
            this.userList = data_base.getUsersListByGroup(userList).getUsers();
            group_choice_box.setValue(userList);
            this.currentGroup = userList;
        } else if (data_base.doesTheGroupExist(currentGroup) > 0) {
            this.userList = data_base.getUsersListByGroup(currentGroup).getUsers();
            group_choice_box.setValue(currentGroup);
        } else {
            group_choice_box.setValue(DEFAULT_GROUP.getTitle());
            this.currentGroup = DEFAULT_GROUP.getTitle();
            group_choice_box.setValue(DEFAULT_GROUP.getTitle());
        }
    }

    public void returnToCurrentGroup(){
        this.userList = data_base.getUsersListByGroup(currentGroup).getUsers();
        group_choice_box.setValue(currentGroup);
    }

    //создает список пользователей из файла (метод не переведен на класс User
    private List<String> getUserListFromFile() {
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

    public List<User> getUserList(){
        return userList;
    }

    //возвращает список пользователей. Если список не был сформирован возвращает единственное значение
    //"Список не найден"
//    public List<String> getUserList() {
//        if (userList == null) {
//            userList = new ArrayList<>(Collections.singletonList("Пользователи не найдены"));
//        }
//        return userList;
//    }

//    public void saveUserList() {
//        userList = userList.stream().sorted(Comparator.comparing(String::format)).collect(Collectors.toList());
//        if (!panelMode.equals("system")) {
//            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
//                for (String u : userList) {
//                    writer.append(u + "\n");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void addUserToDataBase(List<User> users, String group) {
        users.forEach(s -> {
            if (!userList.contains(s)) {
                userList.add(s);
                data_base.addUser(s, group);
            }
        });
    }

    public void deleteFromLocalList(List<User> users) {
        users.forEach(s -> userList.remove(s));
    }

    //загрузка списка пользователей из системы
    public List<User> getUserListFromSystem() {
        List<User> list = new ArrayList<>();
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
                User user = new User();
                user.setName(Dispatch.call(item, "Name").toString());
                list.add(user);
            }
        } catch (UnsatisfiedLinkError e){
            AlertWindowStage alert = new AlertWindowStage("Не обнаружена библиотека jacob-1.20-x64/32.dll");
            alert.showAndWait();
        }
        return list;
    }

}
