package neflo.dev.mapper;

import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.entity.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "groups", ignore = true)
    void updateEntity(@MappingTarget UserModel userModel, UserDTO dto);

    @Mapping(target = "groups", ignore = true)
    UserModel dtoToEntity(UserDTO dto);

    UserDTO entityToDTO(UserModel entity);

}
