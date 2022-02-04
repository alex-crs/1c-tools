package entities.configStructure;

import entities.Groups;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;
import java.util.regex.Matcher;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "Config")
@NoArgsConstructor
@Data
public class Base extends VirtualTree {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic
    private int id;

    @Column
    private String baseId = "";

    @Column(name = "baseName")
    private String elementName;

    @Column
    private String connect;

    @Column
    private String clientConnectionSpeed = "Normal";

    @Column
    private String version = "";

    @Column(name = "folder")
    private String path; //проверить мэппинг в базе данных

    @Column
    private String orderInList = "";

    @Column
    private String orderInTree = "";

    @Column
    private int external = 0;

    @Column
    private String app = "";

    @Column
    private int wa = 1;

    @Column
    private String defaultApp = "";

    @Column
    private int wsa = 1;

    @Column
    private int useProxy = 0;

    @Column
    private String pSrv = "";

    @Column
    private int pPort = 0;

    @Column
    private String pUser = "";

    @Column
    private String pPasswd = "";

    @Column
    private String appArch = "";

    public Base(String elementName) {
        this.elementName = elementName;
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "Config_full_info",
            joinColumns = @JoinColumn(name = "config_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Collection<Groups> groups;

    public void addConfigParameter(String parameter) {
        String[] paramArray = parameter.split("=", 2);
        switch (paramArray[0]) {
            case "Connect": {
                connect = paramArray[1];
                setFolder(false);
                break;
            }
            case "ClientConnectionSpeed": {
                clientConnectionSpeed = paramArray[1];
                break;
            }
            case "DefaultVersion":
            case "Version": {
                version = paramArray[1];
                break;
            }
            case "Folder": {
                path = paramArray[1];
                break;
            }
            case "ID": {
                baseId = paramArray[1];
                break;
            }
            case "OrderInList": {
                orderInList = paramArray[1];
                break;
            }
            case "OrderInTree": {
                orderInTree = paramArray[1];
                break;
            }
            case "External": {
                external = Integer.parseInt(paramArray[1]);
                break;
            }
            case "App": {
                app = paramArray[1];
                break;
            }
            case "WA": {
                wa = Integer.parseInt(paramArray[1]);
                break;
            }
            case "DefaultApp": {
                defaultApp = paramArray[1];
                break;
            }
            case "WSA": {
                wsa = Integer.parseInt(paramArray[1]);
                break;
            }
            case "UseProxy": {
                useProxy = Integer.parseInt(paramArray[1]);
                break;
            }
            case "PSrv": {
                pSrv = paramArray[1];
                break;
            }
            case "PPort": {
                pPort = Integer.parseInt(paramArray[1]);
                break;
            }
            case "PUser": {
                pUser = paramArray[1];
                break;
            }
            case "PPasswd": {
                pPasswd = paramArray[1];
                break;
            }
        }
    }

    @Override
    public String toString() {
        return elementName;
    }
}
