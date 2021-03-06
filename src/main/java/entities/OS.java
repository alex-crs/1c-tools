package entities;

public interface OS {
    void setHomeDirectory(String path);

    String getHomeDirectory();

    //собирает готовый путь до папки с 1С (собирает системные пути с именем пользователя)
    String cv8cachePathConstructor(String userName);

    String cv82cachePathConstructor(String userName);

    String cv83cachePathConstructor(String userName);

    String basePathConstructor(String userName);

    String getLocationConfig(String userName);

    String ceStartPathConstructor(String userName);

    String getCEStartDirectory(String userName);

    String getPlatformConfigDirectory(String userName);
}
