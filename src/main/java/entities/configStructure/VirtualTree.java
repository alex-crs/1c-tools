package entities.configStructure;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import settings.BaseConfig;

import java.util.*;

@Data
public class VirtualTree {
    private final List<VirtualTree> elements;
    private String elementName;
    private boolean folder;
    private StringBuilder path;
    private boolean isExpand;


    public VirtualTree() {
        this.elementName = "";
        this.elements = new ArrayList<>();
        this.folder = true;
        this.path = new StringBuilder();
    }

    public VirtualTree(String elementName) {
        this.elementName = elementName;
        this.elements = new ArrayList<>();
        this.folder = true;
        this.path = new StringBuilder();
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }

    public void setPath(String path) {
        this.path.delete(0,path.length());
        this.path.append(path);
    }

    public void setElementName(String name) {
        this.elementName = name;
    }

    public String getPath() {
        return path.toString();
    }


    //рекурсивно ищет элемент по всему древу
    public VirtualTree findFromTree(VirtualTree element) {
        VirtualTree foundElement = null;
        for (VirtualTree e : elements) {
            if (e.getElementName().equals(element.getElementName())) {
                foundElement = e;
                break;
            }
            if (foundElement == null && e.folder) {
                foundElement = e.findFromTree(element);
            }
        }
        return foundElement;
    }

    public void sortAllElements() {
        sortCurrentElements();
        for (VirtualTree e : elements) {
            if (e.isFolder()) {
                e.sortAllElements();
            }
        }
    }

    private void sortCurrentElements() {
        elements.sort(Comparator.comparing(VirtualTree::getElementName));
    }

    /*Изменяет текущее состояние раскрытости древа. Работает рекурсивно. По факту достаточно медленный
    в дальнейшем можно заменить рекурсию на поиск по древу: так как у каждого элемента есть свой путь
    расположения*/
    public void changeCurrentExpandStatement(VirtualTree element, boolean expandStatus) {
        for (VirtualTree e : elements) {
            if (e.getElementName().equals(element.getElementName())) {
                e.setExpand(expandStatus);
                break;
            }
            if (e.folder) {
                e.changeCurrentExpandStatement(element, expandStatus);
            }
        }
    }

    //удаляет элемент из древа конфигураций
    public int removeElement(VirtualTree element) {
        int answer = 0;
        for (VirtualTree e : elements) {
            if (e.getElementName().equals(element.getElementName())) {
                elements.remove(element);
                answer = 1;
                break;
            }
            if (e.folder) {
                answer = e.removeElement(element);
            }
            if (answer > 0) {
                break;
            }
        }
        return answer;
    }

    //проверяет элемент добавляемый элемент на наличие дубликатов, подготавливает и добавляет элемент
    public int inspectAndAddTreeElement(String path, VirtualTree element) {
        if (findFromTree(element) == null) {
            pathNormalizer(element, path);
            addVirtualElement(pathBuilder(path), element);
            return 1;
        } else {
            return -1;
        }
    }

    //добавляет виртуальный элемент независимо от того папка он или конфигурация
    private void addVirtualElement(String[] path, VirtualTree element) {
        Optional<VirtualTree> currentObject = elements.stream()
                .filter(virtualTree -> virtualTree.getElementName().equals(Objects.requireNonNull(path[0])))
                .findFirst();
        if (currentObject.isPresent()) {
            currentObject.get().addVirtualElement(pathCut(path), element);
        } else if (path.length > 0 && path[0].length() > 0) {
            VirtualTree treeObject = new Folder(path[0]);
            treeObject.setPath(getPath() + (elementName != null ? "/" + elementName : ""));
            treeObject.addVirtualElement(pathCut(path), element);
            elements.add(treeObject);
        } else if (findElement(element) > 0) {
            elements.add(element);
        }
    }

    //восстанавливает пути до объекта и вложенных в него объектов относительно обновленного древа
    private void pathNormalizer(VirtualTree element, String path) {
        if (element.isFolder()) {
            element.getElements().forEach(virtualTree -> {
                if (virtualTree.isFolder()) {
                    pathNormalizer(virtualTree, path + "/" + element.getElementName());
                } else {
                    virtualTree.setPath(path + "/" + element.getElementName());
                }
            });
        }
        element.setPath(path.length() > 0 ? path : "");
    }

    private int findElement(VirtualTree element) {
        Optional<VirtualTree> findElement = elements.stream()
                .filter(virtualTree -> virtualTree.getElementName().equals(Objects.requireNonNull(element.elementName)))
                .findFirst();
        return findElement.isPresent() ? -1 : 1;
    }

    //строит список конфигураций с учетом иерархий (с папками и конфигурациями)
    public TreeItem<VirtualTree> treeBuilder() {
        TreeItem<VirtualTree> parent = treeItemGenerator(this);
        parent.expandedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                isExpand = parent.isExpanded();
                BaseConfig.changeCurrentExpandStatement(VirtualTree.this, isExpand);
            }
        });
        elements.forEach(virtualTree -> {
            if (virtualTree.elements.size() != 0) {
                parent.getChildren().add(virtualTree.treeBuilder());
            } else {
                parent.getChildren().add(treeItemGenerator(virtualTree));
            }
            parent.setExpanded(isExpand);
        });
        return parent;
    }

    //строит древо папок без конфигураций
    public TreeItem<VirtualTree> folderTreeBuilder() {
        TreeItem<VirtualTree> parent = treeItemGenerator(this);
        elements.forEach(virtualTree -> {
            if (virtualTree.elements.size() != 0 && virtualTree.isFolder()) {
                parent.getChildren().add(virtualTree.folderTreeBuilder());
            } else if (virtualTree.isFolder()) {
                parent.getChildren().add(treeItemGenerator(virtualTree));
            }
            parent.setExpanded(isExpand);
        });
        return parent;
    }

    /*Возвращает TreeItem из объекта типа VirtualTree с иконкой папочки или конфигурации*/
    private TreeItem<VirtualTree> treeItemGenerator(VirtualTree element) {
        TreeItem<VirtualTree> item = new TreeItem<>(element);
        if (element.folder) {
            item.setGraphic(new ImageView(new Image("images/folder.png")));
        } else {
            item.setGraphic(new ImageView(new Image("images/config.png")));
        }
        return item;
    }

    public List<VirtualTree> getElements() {
        return elements;
    }

    //обработчики путей
    private String[] pathCut(String[] path) {
        if (path.length > 1) {
            return Arrays.copyOfRange(path, 1, path.length);
        } else {
            return new String[]{""};
        }
    }

    //отстраивает массив пути из строки пути
    private String[] pathBuilder(String path) {
        return path.replaceAll("//", "/").substring(1).split("/");
    }
    //-----------------

    //возвращает имя элемента
    public String getElementName() {
        return elementName;
    }

    @Override
    public String toString() {
        return elementName;
    }
}
