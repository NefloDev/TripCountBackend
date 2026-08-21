package neflo.dev.model.entity;

import jakarta.persistence.*;
import lombok.*;
import neflo.dev.model.dto.group.GroupDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GRP_GROUPS")
public class GroupModel implements Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "GROUP_CODE", nullable = false, unique = true, length = 9)
    private String groupCode;

    @Lob
    @Column(name = "PFP")
    private String pfp;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "GRP_ID")
    private List<TripModel> trips;

    @ManyToMany(mappedBy = "groups")
    private List<UserModel> members;

    public void addMember(UserModel user){
        if (members == null){
            members = new ArrayList<>();
        }
        members.add(user);
        user.getGroups().add(this);
    }

    public void addTrip(TripModel trip){
        if (trips == null){
            trips = new ArrayList<>();
        }
        trips.add(trip);
        trip.setGroup(this);
    }

    @Override
    public boolean isNew() {
        return id == null;
    }

    public boolean equalsDto(GroupDTO dto) {
        if (dto == null) return false;

        return new EqualsBuilder()
                .append(name, dto.name())
                .isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        GroupModel that = (GroupModel) o;

        return new EqualsBuilder().append(id, that.id).append(name, that.name).append(groupCode, that.groupCode).append(pfp, that.pfp).append(trips, that.trips).append(members, that.members).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(name).append(groupCode).append(pfp).append(trips).append(members).toHashCode();
    }
}
