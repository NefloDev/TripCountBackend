package neflo.dev.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "GRP_TRIPS")
public class TripModel implements Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @OneToOne()
    @JoinColumn(name = "GRP_ID", referencedColumnName = "ID")
    private GroupModel group;

    @OneToOne()
    @JoinColumn(name = "DRIVER", referencedColumnName = "ID")
    private UserModel driver;

    @Column(name = "DATE")
    private LocalDate date;

    @Column(name = "DURATION_MINUTES")
    private Integer durationMinutes;

    @Column(name = "ORIGIN")
    private String origin;

    @Column(name = "DESTINATION")
    private String destination;

    @Column(name = "NOTES")
    private String notes;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
