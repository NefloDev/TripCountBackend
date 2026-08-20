package neflo.dev.mapper;

import neflo.dev.model.dto.group.GroupMemberDTO;
import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.dto.user.UserResponseDTO;
import neflo.dev.model.entity.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(@MappingTarget UserModel userModel, UserDTO dto);

    UserResponseDTO entityToResponseDTO(UserModel entity);

    GroupMemberDTO entityToGroupMemberDTO(UserModel entity);

}
