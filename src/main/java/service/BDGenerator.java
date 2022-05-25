package service;

import org.apache.log4j.Logger;

import java.sql.*;

public class BDGenerator {
    private static Connection connection;
    private static Statement statement;
    private static final Logger LOGGER = Logger.getLogger(BDGenerator.class);

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:base.db");
            statement = connection.createStatement();
            LOGGER.info("DataBase connection successfully. Connection settings: " + statement.getConnection());
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error("Ошибка подключения к базе данных:", e);
        }
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.error("Ошибка отключения от базы данных:", e);
        }
    }

    public static int checkBD() {
        String query = "select * from version_info where id=?;";
        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "1");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String result = rs.getString(2);
                return Integer.parseInt(result);
            }
        } catch (SQLException e) {
            LOGGER.error("Файл базы данных пустой");
        }
        return -1;
    }

    public static void createBD() {
        int query1 = createConfigTable();
        int query2 = createGroupsTable();
        int query3 = createConfigFullInfoTable();
        int query4 = createUserTable();
        int query5 = addDefaultGroup();
        int query6 = createVersionTable();
        int query7 = addBDVersion();
        if (query1 + query2 + query3 + query4 + query5 + query6 + query7 == 7) {
            LOGGER.info("База base.bd успешно создана;");
        } else {
            LOGGER.info("Программа не смогла создать base.bd;");
        }
    }

    private static int createConfigTable() {
        String configTable = "create table Config\n" +
                "(\n" +
                "    id                    INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    baseName              STRING,\n" +
                "    baseId                STRING,\n" +
                "    connect               STRING,\n" +
                "    clientConnectionSpeed STRING,\n" +
                "    version               STRING,\n" +
                "    folder                STRING,\n" +
                "    orderInList           STRING,\n" +
                "    orderInTree           STRING,\n" +
                "    external              INT,\n" +
                "    app                   STRING,\n" +
                "    wa                    INT,\n" +
                "    defaultApp            STRING,\n" +
                "    wsa                   INT,\n" +
                "    useProxy              INT(2),\n" +
                "    pSrv                  STRING,\n" +
                "    pPort                 INT(5),\n" +
                "    pUser                 STRING,\n" +
                "    pPasswd               STRING,\n" +
                "    appArch               STRING(10)\n" +
                ");";

        try {
            PreparedStatement ps = connection.prepareStatement(configTable);
            ps.executeUpdate();
            LOGGER.info("Создана таблица Config");
            return 1;
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблицы Config:", e);
            return 0;
        }
    }

    private static int createGroupsTable() {
        String groupTable = "create table Groups\n" +
                "(\n" +
                "    id   INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    name STRING\n" +
                "        unique\n" +
                ");";
        try {
            PreparedStatement ps = connection.prepareStatement(groupTable);
            ps.executeUpdate();
            LOGGER.info("Создана таблица Группы");
            return 1;
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблицы Group:", e);
            return 0;
        }
    }

    private static int createConfigFullInfoTable() {
        String configFullInfoTable = "create table Config_full_info\n" +
                "(\n" +
                "    group_id  STRING\n" +
                "        references Groups\n" +
                "            on update cascade on delete cascade,\n" +
                "    config_id INTEGER\n" +
                "        references Config\n" +
                "            on update cascade on delete cascade\n" +
                ");";
        try {
            PreparedStatement ps = connection.prepareStatement(configFullInfoTable);
            ps.executeUpdate();
            LOGGER.info("Создана таблица Информация о конфигурации");
            return 1;
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблицы ConfigFullTable:", e);
            return 0;
        }
    }

    private static int createUserTable() {
        String userTable = "create table User\n" +
                "(\n" +
                "    id       INTEGER\n" +
                "        primary key autoincrement,\n" +
                "    name     INTEGER,\n" +
                "    group_id INTEGER\n" +
                "        references Groups\n" +
                "            on update cascade on delete cascade\n" +
                ");";
        try {
            PreparedStatement ps = connection.prepareStatement(userTable);
            ps.executeUpdate();
            LOGGER.info("Создана таблица Пользователи");
            return 1;
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблицы User:", e);
            return 0;
        }
    }

    private static int createVersionTable() {
        String versionTable = "create table version_info\n" +
                "(\n" +
                "    id      integer\n" +
                "        constraint version_info_pk\n" +
                "            primary key autoincrement,\n" +
                "    version integer\n" +
                ");";
        try {
            PreparedStatement ps = connection.prepareStatement(versionTable);
            ps.executeUpdate();
            LOGGER.info("Создана таблица Версия базы данных");
            return 1;
        } catch (SQLException e) {
            LOGGER.error("Ошибка создания таблицы Version:", e);
            return 0;
        }
    }

    private static int addDefaultGroup() {
        try {
            String query = "INSERT INTO Groups (name) VALUES (?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "Группа по умолчанию");
            int result = ps.executeUpdate();
            LOGGER.info(String.format(result == 0 ? "Не удалось добавить Группу по умолчанию" : "Группа по умолчанию успешно добавлена"));
            return result;
        } catch (SQLException e) {
            LOGGER.error("Ошибка добавления Группы по умолчанию в базу данных:", e);
            return 0;
        }
    }

    private static int addBDVersion() {
        try {
            String query = "INSERT INTO version_info (version) VALUES (?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, "1");
            int result = ps.executeUpdate();
            LOGGER.info(String.format(result == 0 ? "Не удалось добавить версию базы данных" : "Версия базы данных успешно добавлена"));
            return result;
        } catch (SQLException e) {
            LOGGER.error("Ошибка добавления версии базы данных:", e);
            return 0;
        }
    }

}
