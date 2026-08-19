package neflo.dev.model.relation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import neflo.dev.model.GroupModel;
import neflo.dev.model.UserModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "REL_GROUP_MEMBERS")
public class GroupMember {

    @Id
    @OneToOne()
    @JoinColumn(name = "USR_ID", referencedColumnName = "ID")
    private UserModel member;

    @Id
    @OneToOne()
    @JoinColumn(name = "GRP_ID", referencedColumnName = "ID")
    private GroupModel group;

    @Column(name = "TIME_BALANCE")
    private Integer timeBalance;

}
