package neflo.dev.repository;

import neflo.dev.model.entity.GroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<GroupModel, UUID> {

    @Query("select g from GroupModel g inner join g.members members where g.id = :id and members.id in :ids")
    Optional<GroupModel> findByIdAndMembers_IdIn(@Param("id") UUID id, @Param("ids") Collection<UUID> ids);

}
