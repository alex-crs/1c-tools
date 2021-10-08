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
import java.util.ArrayList;
import java.util.List;

public class UserList {
    private final Logger LOGGER = Logger.getLogger(UserList.class);
    private List<String> userList;

    public UserList() {
        this.userList = createUserList();
    }

    public List<String> getUserList() {
        return userList;
    }

    //загрузка списка пользователей из системы
    public List<String> createUserList() {
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

    //чтение списка пользователей из файла
    public List<String> readUserList() {
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
}
