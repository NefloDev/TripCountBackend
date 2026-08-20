package neflo.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.DatabaseException;
import neflo.dev.exceptions.NoEntitiesFoundException;
import neflo.dev.exceptions.ValidationException;
import neflo.dev.mapper.GroupMapper;
import neflo.dev.mapper.UserMapper;
import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.dto.user.UserRequestDTO;
import neflo.dev.model.entity.UserModel;
import neflo.dev.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {

    private static final String CLASS_PATH = "TripCount.UserService";

    private final UserRepository repository;
    private final UserMapper mapper;
    private final GroupMapper groupMapper;

    public UserDTO getUserByEmail(UserRequestDTO requestDTO){
        log.info("{}.getUserByEmail() >> requestDTO :: {}", CLASS_PATH, requestDTO);

        UserModel repositoryResponse = repository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", String.format("No user found with email %s.", requestDTO.email())));

        UserDTO responseDTO = mapper.entityToDTO(repositoryResponse);

        log.info("{}.getUserByEmail() >> responseDTO :: {}", CLASS_PATH, responseDTO);
        return responseDTO;
    }

    public void insertUser(UserDTO dto){
        log.info("{}.insertUser() >> dto :: {}", CLASS_PATH, dto);

        if (repository.findByEmail(dto.email()).isPresent()){
            throw new ValidationException("email-exists", "A user with that email already exists.");
        }

        UserModel userModel = mapper.dtoToEntity(dto);

        userModel = repository.save(userModel);

        if (userModel.getId() == null){
            throw new DatabaseException("user-not-saved", "User couldn't be saved at the moment, try again later.");
        }

        log.info("{}.insertUser() >> user saved successfully", CLASS_PATH);
    }

    public void updateUser(UUID uuid, UserDTO dto){
        log.info("{}.updateUser() >> uuid :: {} -- dto :: {}", CLASS_PATH, uuid, dto);

        UserModel repositoryResponse = repository.findById(uuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "No user found."));

        mapper.updateEntity(repositoryResponse, dto);

        repositoryResponse = repository.save(repositoryResponse);

        if (!repositoryResponse.equalsDto(dto)){
            throw new DatabaseException("user-not-updated", "User couldn't be updated at the moment, try again later.");
        }

        log.info("{}.insertUser() >> user updated successfully", CLASS_PATH);
    }

    public void deleteUser(UUID uuid){
        log.info("{}.deleteUser() >> uuid :: {}", CLASS_PATH, uuid);

        repository.deleteById(uuid);

        if (repository.findById(uuid).isPresent()){
            throw new DatabaseException("user-not-deleted", "User couldn't be deleted at the moment, try again later.");
        }
        log.info("{}.deleteUser() >> user deleted successfully", CLASS_PATH);
    }

    public List<GroupDTO> getUserGroups(UUID uuid) {
        log.info("{}.getUserGroups() >> uuid :: {}", CLASS_PATH, uuid);

        UserModel repositoryResponse = repository.findById(uuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "No user found."));
        log.info("{}.getUserGroups() >> user found", CLASS_PATH);

        List<GroupDTO> userGroups = repositoryResponse.getGroups().stream().map(groupMapper::entityToDTO).toList();
        log.info("{}.getUserGroups() >> userGroups :: {}", CLASS_PATH, userGroups.stream().map(GroupDTO::name).toList());

        return userGroups;
    }

}
