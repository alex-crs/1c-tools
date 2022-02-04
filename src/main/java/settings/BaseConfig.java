package settings;


import entities.OS;
import entities.configStructure.Base;
import entities.configStructure.VirtualTree;
import javafx.scene.control.TreeItem;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BaseConfig {
    private static final Logger LOGGER = Logger.getLogger(BaseConfig.class);
    private static final VirtualTree configTree = new VirtualTree();

    //читает конфигурацию из файла в системе
    public static void readConfigParameter(String userName, OS operatingSystem) {
        LOGGER.info(String.format("Чтение коллекции конфигураций пользователя [%s]", userName));
        File file = new File(operatingSystem.basePathConstructor(userName));
        int stringCount = 0;
        StringBuilder string = new StringBuilder();
        Base baseElement = null;
        if (file.exists() && file.length() > 0) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    string.append(reader.readLine());
                    if (string.toString().charAt(0) == 65279) { //это ZERO WIDTH NO-BREAK SPACE по сути это char -
                        // символ кодировки UTF-8 без BOM (Byte order Mark) знак порядка байтов
                        string.deleteCharAt(0); //удалим его и добавим при записи
                    }
                    if (string.toString().startsWith("[")) {
                        if (baseElement != null) {
                            configTree.addTreeElement(baseElement.getPath(), baseElement);
                        }
                        baseElement = new Base();
                        baseElement.setElementName(string.toString()
                                .replaceAll("]", "")
                                .replaceAll("\\[", ""));
                    } else {
                        assert baseElement != null;
                        baseElement.addConfigParameter(string.toString());
                    }
                    string.delete(0, string.length());
                    stringCount++;
                }
                assert baseElement != null;
                configTree.addTreeElement(baseElement.getPath(), baseElement);
                LOGGER.info(String.format("Коллекция конфигураций пользователя [%s] загружена. Прочитано [%s] строк", userName, stringCount));
                configTree.sortAllElements();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //возвращает древо конфигураций
    public static TreeItem<VirtualTree> returnConfigStructure() {
        return configTree.treeBuilder();
    }

    //очищает древо конфигураций
    public static void clearTree() {
        configTree.getElements().clear();
    }

    //добавляет элемент в древо конфигураций
    public static int addElement(TreeItem<VirtualTree> sourcePath, VirtualTree addingElement) {
        int answer = -1;
        if (sourcePath.getValue().isFolder()) {
            answer = configTree.addTreeElement(sourcePath.getValue().getPath() + "/"
                    + sourcePath.getValue().getElementName(), addingElement);
        } else {
            answer = configTree.addTreeElement(sourcePath.getParent().getValue().getPath()
                    + "/" + sourcePath.getParent().getValue().getElementName(), addingElement);
        }
        return answer;
    }

    //удаляет элемент из древа конфигураций
    public static void deleteElement(VirtualTree element) {
        configTree.removeElement(element);
    }

    //изменяет isExpand статус элемента (раскрыт или закрыт элемент
    public static void changeCurrentExpandStatement(VirtualTree element, boolean status) {
        configTree.changeCurrentExpandStatement(element, status);
    }

    public static void changeElement(VirtualTree oldElement, VirtualTree newElement){
        LOGGER.info("Изменение элемента");
        configTree.changeElement(oldElement, newElement);
        LOGGER.info("Элеент изменен");

    }
}
