package entities;

import java.util.HashMap;

public class BaseElement {
    String baseName;
    HashMap<String, String> baseConfig;

    public BaseElement(String baseName) {
        this.baseName = baseName;
        baseConfig = new HashMap<>();
    }

    public void addConfigParameter(String parameter) {
        String[] paramArray = parameter.split("=", 2);
        baseConfig.put(paramArray[0], paramArray[1]);
    }

    public String getBaseName() {
        return baseName;
    }
}
