package entities.PlatformParams;

import lombok.Data;

@Data
public class DefaultVersionObject {
    private String bitDepth;
    private String targetVersion;
    private String usedVersion;

    public String returnBitDepthView() {
        if (bitDepth.equals("x86")) {
            return "32 (x86)";
        }
        if (bitDepth.equals("x86_prt")) {
            return "Приоритет 32 (x86)";
        }
        if (bitDepth.equals("x86_64")) {
            return "64 (x86_64)";
        }
        if (bitDepth.equals("x86_64_prt")) {
            return "Приоритет 64 (x86_64)";
        }
        return "";
    }

    public String returnParam() {
        StringBuilder param = new StringBuilder();
        param.append("DefaultVersion=");
        if (targetVersion != null) {
            param.append(targetVersion).append("-");
        }
        if (usedVersion != null) {
            param.append(usedVersion);
        }
        if (bitDepth != null) {
            param.append(";").append(bitDepth);
        }
        return param.toString();
    }

    public DefaultVersionObject(String param) {
        String[] firstParams = param.split(";");
        if (firstParams.length > 1) {
            this.bitDepth = firstParams[1];
        }
        String[] secondParams = firstParams[0].split("-");
        if (secondParams.length > 1) {
            this.targetVersion = secondParams[0];
            this.usedVersion = secondParams[1];
        } else {
            this.usedVersion = secondParams[0];
        }
    }

    @Override
    public String toString() {
        return String.format("Для версии %s используется %s, %s",
                targetVersion,
                usedVersion,
                bitDepth != null ? returnBitDepthView() : "");
    }
}
