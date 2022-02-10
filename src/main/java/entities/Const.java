package entities;


public enum Const {
    EMPTY("Пустой запрос"),

    //константы для групп и пользователей
    CREATE_USER("Создать пользователя"),
    DEFAULT_GROUP("Группа по умолчанию"),
    CREATE_GROUP("Создать группу"),
    RENAME_GROUP("Изменить имя группы"),
    DELETE_GROUP("Удалить группу"),
    GROUPS_DELIMITER("------- Группы: -------"),

    //константы для конфигураций
    CREATE_TREE_ELEMENT("Создать элемент"),
    EDIT_TREE_FOLDER("Изменить имя папки"),
    EDIT_TREE_CONFIG("Изменить параметры конфигурации"),
    CHECK_UNSAVED_DATA("Имеются несохраненные данные"),
    DELETE_ELEMENT("Удалить элемент?");

    private final String title;

    Const(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    //возвращает соответствующий экземпляр Enum, если не находит, бросает исключение
    public static Const getConst(String title) {
        if (title != null) {
            for (Const v : Const.values()) {
                if (v.getTitle().equals(title)) {
                    return v;
                }
            }
            throw new IllegalArgumentException();
        }
        return EMPTY;
    }
}
