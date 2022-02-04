package entities;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GroupDTO {
    String groupName;
    List<User> users;

    public GroupDTO getGroupDTO(Groups groups) {
        this.groupName = groups.getName();
        users = groups.getUserList();
        return this;
    }
}
