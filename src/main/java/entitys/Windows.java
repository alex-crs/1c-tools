package entitys;

import java.io.File;

public class Windows implements OS {

    StringBuilder homeDirectory;
    StringBuilder tempPath = new StringBuilder().append("\\AppData\\Local\\1C\\");
    StringBuilder baseConfig = new StringBuilder().append("\\AppData\\Roaming\\1C\\1CEStart\\ibases.v8i");

    public StringBuilder getTempPath() {
        return tempPath;
    }

    public Windows() {
        setHomeDirectory();
    }

    @Override
    public void setHomeDirectory(String path) {
        if (path == null || path.length() == 0) {
            homeDirectory = findHomeDirectoryPath();
        } else {
            homeDirectory.append(path).append(File.separator);
        }
    }

    public void setHomeDirectory() {
        homeDirectory = findHomeDirectoryPath();
    }

    @Override
    public String getHomeDirectory() {
        return null;
    }

    private StringBuilder findHomeDirectoryPath() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("user.home"));
        sb.delete(sb.length() - (System.getenv("username").length()), sb.length());
        return sb;
    }

    //собирает готовый путь до папки с 1С (собирает системные пути с именем пользователя)
    @Override
    public String cachePathConstructor(String userName) {
        return homeDirectory + userName + tempPath;
    }

    public String basePathConstructor(String userName) {
        return homeDirectory + userName + baseConfig;
    }
}
