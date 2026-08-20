package neflo.dev.mapper;

import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.entity.GroupModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDTO entityToDTO(GroupModel entity);

    @Mapping(target = "groupCode", ignore = true)
    void updateEntity(@MappingTarget GroupModel entity, GroupDTO dto);

}
