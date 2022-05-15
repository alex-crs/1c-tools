package entities;

public interface OS {
    void setHomeDirectory(String path);

    String getHomeDirectory();

    String cachePathConstructor(String userName);

    String basePathConstructor(String userName);

    String cv8StartConfigPathConstructor(String userName);

    String ceStartPathConstructor(String userName);
}
