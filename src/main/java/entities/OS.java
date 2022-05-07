package entities;

public interface OS {
    void setHomeDirectory(String path);

    String getHomeDirectory();

    String cachePathConstructor(String userName);

    String basePathConstructor(String userName);

    String platformConfigPathConstructor(String userName);

    String ceStartPathConstructor(String userName);
}
