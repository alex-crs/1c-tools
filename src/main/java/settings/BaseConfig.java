package settings;


import entities.OS;
import entities.configStructure.Base;
import entities.configStructure.VirtualTree;
import javafx.scene.control.TreeItem;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                            configTree.inspectAndAddTreeElement(baseElement.getPath(), baseElement.returnTrueCurrentObject());
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
                configTree.inspectAndAddTreeElement(baseElement.getPath(), baseElement);
                LOGGER.info(String.format("Коллекция конфигураций пользователя [%s] загружена. Прочитано [%s] строк", userName, stringCount));
                configTree.sortAllElements();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //записывает в текущее древо конфигураций в файл
    public static void writeConfigToFile(String userName, OS operatingSystem) {
        File file = new File(operatingSystem.basePathConstructor(userName));
        File cEStartDir = new File(operatingSystem.getCEStartDirectory(userName));
        ArrayList<String> list = configTree.virtualTreeAsListCollector();
        try {
            if (!cEStartDir.exists()){
                Files.createDirectories(Paths.get(String.valueOf(cEStartDir)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fis = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter((new OutputStreamWriter(fis, StandardCharsets.UTF_8)))) {

            writer.write(65279);
            for (String l : list) {
                writer.write(l);
                writer.newLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //возвращает древо конфигураций
    public static TreeItem<VirtualTree> returnConfigStructure() {
        return configTree.treeBuilder();
    }

    //возвращает древо папок без конфигураций
    public static TreeItem<VirtualTree> returnFolderStructure() {
        return configTree.folderTreeBuilder();
    }

    //очищает древо конфигураций
    public static void clearTree() {
        configTree.getElements().clear();
    }

    //добавляет элемент в древо конфигураций
    public static int addElement(TreeItem<VirtualTree> sourcePath, VirtualTree addingElement) {
        int answer = -1;
        String path;
        if (sourcePath.getValue().isFolder()) {
            path = buildPathRelativeToObject(sourcePath)
                    + "/" + sourcePath.getValue().getElementName();
            answer = configTree.inspectAndAddTreeElement(path, addingElement);
        } else {
            path = buildPathRelativeToObject(sourcePath.getParent())
                    + "/" + sourcePath.getParent().getValue().getElementName();
            answer = configTree.inspectAndAddTreeElement(path, addingElement);
        }
        return answer;
    }

    //отстраивает путь до объекта относительно корня TreeItem
    private static StringBuilder buildPathRelativeToObject(TreeItem<VirtualTree> element) {
        StringBuilder path = new StringBuilder();
        ArrayList<String> list = new ArrayList<>();
        TreeItem<VirtualTree> currentElement = element;
        while (true) {
            TreeItem<VirtualTree> thisElement = currentElement.getParent();
            if (thisElement != null) {
                list.add(0, thisElement.getValue().getElementName());
                currentElement = thisElement;
            } else {
                break;
            }
        }
        list.forEach(s -> path.append(s.length() > 0 ? "/" + s : ""));
        return path;
    }

    //удаляет элемент из древа конфигураций
    public static void deleteElement(VirtualTree element) {
        configTree.removeElement(element);
    }

    public static void moveElement(VirtualTree element, TreeItem<VirtualTree> sourcePath) {
        configTree.removeElement(element);
        addElement(sourcePath, element);
    }

    //изменяет isExpand статус элемента (раскрыт или закрыт элемент
    public static void changeCurrentExpandStatement(VirtualTree element, boolean status) {
        configTree.changeCurrentExpandStatement(element, status);
    }

    public static ArrayList<Base> returnList() {
        ArrayList<Base> list = new ArrayList<>();
        for (VirtualTree l : configTree.getElements()) {
            if (!l.isFolder()) {
                list.add((Base) l);
            }
        }
        return list;
    }
}
