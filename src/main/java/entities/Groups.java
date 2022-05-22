package entities;

import entities.configStructure.Base;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Groups")
public class Groups {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic
    private int id;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "Config_full_info",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "config_id")
    )
    private Collection<Base> baseElements;


    @LazyCollection(LazyCollectionOption.FALSE) //что это такое?
    @OneToMany(mappedBy = "groups", cascade = CascadeType.ALL)
    private List<User> users;

    public List<User> getUserList(){
        return users;
    }
}
