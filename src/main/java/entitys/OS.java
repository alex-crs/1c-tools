package entitys;

public interface OS {
    void setHomeDirectory(String path);

    String getHomeDirectory();

    public String cachePathConstructor(String userName);

    public String basePathConstructor(String userName);
}
