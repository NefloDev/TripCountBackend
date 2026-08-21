package neflo.dev.mapper;

import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.entity.GroupModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface GroupMapper {

    GroupDTO entityToDTO(GroupModel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groupCode", ignore = true)
    void updateEntity(@MappingTarget GroupModel entity, GroupDTO dto);

    default String optionalToString(Optional<String> source) {
        return source.orElse(null);
    }

    default Optional<String> stringToOptional(String source) {
        return Optional.ofNullable(source);
    }

}
