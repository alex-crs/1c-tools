
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;
import com.jacob.com.Variant;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class Start {
    public static void main(String[] args) {
        List<String> excludedFiles = readExcludeList();
//        List<String> userList = createUserList();

        for (String e : excludedFiles) {
            System.out.println(e);
        }
        saveExcludeList(excludedFiles);
        clear1sCache(excludedFiles);
    }

    private static void clear1sCache(List<String> excludedList){
        //Пока только один путь
        File fileDirectory = new File("c:\\Users" + File.separator + "alex_crs" + File.separator + "AppData\\Local\\1C\\1cv8");
        try {
            String[] path = fileDirectory.list();
            for (String e : path) {
                if (!excludedList.contains(e)) {
                    deleteItems(fileDirectory.getPath(), e);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteItems(String path, String fileName) throws IOException {
        Files.walkFileTree(Path.of(path + File.separator + fileName), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static List<String> createUserList() {
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

    private static List<String> readExcludeList() {
        List<String> list = new ArrayList<>();
        File file = new File("ignore.txt");
        int stringCount = 0;
        try (FileInputStream fis = new FileInputStream(file);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            while (reader.ready()) {
                list.add(reader.readLine());
                stringCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private static void saveExcludeList(List<String> excludeList) {
        File file = new File("ignore2.txt");
        int stringCount = 0;
        try (FileOutputStream fis = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fis))) {
            if (!file.exists()) {
                file.createNewFile();
            }
            for (String e : excludeList) {
                writer.write(e + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
