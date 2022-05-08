package entities.PlatformParams;

import lombok.Data;

@Data
public class SharedBase {
    private String paramName;
    private boolean iService;

    public String returnParam() {
        return iService ? "CommonInfoBases=" + paramName : "InternetService=" + paramName;
    }

    public SharedBase(String[] paramName) {
        if (paramName[0].equals("CommonInfoBases")) {
            this.paramName = paramName[1];
        }
        if (paramName[0].equals("InternetService")) {
            this.paramName = paramName[1];
            iService = true;
        }
    }

    @Override
    public String toString() {
        return paramName;
    }
}
