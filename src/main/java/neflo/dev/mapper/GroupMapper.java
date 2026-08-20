package neflo.dev.mapper;

import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.entity.GroupModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDTO entityToDTO(GroupModel entity);

}
