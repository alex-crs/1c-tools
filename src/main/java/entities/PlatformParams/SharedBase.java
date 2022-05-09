package entities.PlatformParams;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class SharedBase {

    @Getter
    private String paramName;

    @Getter
    private boolean iService;

    public String returnParam() {
        return iService ? "CommonInfoBases=" + paramName : "InternetService=" + paramName;
    }

    public SharedBase(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String toString() {
        return paramName;
    }
}
