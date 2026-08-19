package neflo.dev.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GRP_GROUPS")
public class GroupModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Lob
    @Column(name = "PFP", columnDefinition = "BYTEA")
    private byte[] pfp;

    @OneToMany
    @JoinColumn(name = "GRP_ID")
    private List<TripModel> trips;

    @ManyToMany
    @JoinTable(
            name = "REL_GROUP_MEMBERS",
            joinColumns = @JoinColumn(name = "ID"),
            inverseJoinColumns = @JoinColumn(name = "GRP_ID")
    )
    private List<UserModel> groups;

}
