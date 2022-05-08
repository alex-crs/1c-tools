package entities.PlatformParams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class Templates {
    private String configurationTemplatesLocation;

    public String returnParam() {
        return "ConfigurationTemplatesLocation=" + configurationTemplatesLocation;
    }

    @Override
    public String toString() {
        return configurationTemplatesLocation;
    }
}
