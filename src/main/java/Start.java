
import settings.Ignored_objects;
import settings.UserList;

import static handlers.CacheCleaner.*;

public class Start {

    public static void main(String[] args) {
        clearCacheByUser(new UserList(), new Ignored_objects());
    }
}
