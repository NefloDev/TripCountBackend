package neflo.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.DatabaseException;
import neflo.dev.exceptions.NoEntitiesFoundException;
import neflo.dev.mapper.GroupMapper;
import neflo.dev.mapper.UserMapper;
import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.dto.user.UserDTO;
import neflo.dev.model.dto.user.UserResponseDTO;
import neflo.dev.model.entity.UserModel;
import neflo.dev.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
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

    public UserResponseDTO getUserDetail(UUID uuid) {
        log.info("{}.getUserDetail() >> uuid :: {}", CLASS_PATH, uuid);

        UserModel repositoryResponse = repository.findById(uuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));

        log.info("{}.getUserDetail() >> user found :: {}", CLASS_PATH, repositoryResponse);
        return mapper.entityToResponseDTO(repositoryResponse);
    }

    public UserResponseDTO updateUser(UUID uuid, UserDTO dto) {
        log.info("{}.updateUser() >> uuid :: {} -- dto :: {}", CLASS_PATH, uuid, dto);

        UserModel repositoryResponse = repository.findById(uuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));

        mapper.updateEntity(repositoryResponse, dto);
        boolean hasUpdatedPassword = false;
        if (dto.password().isPresent()){
            String password = dto.password().get();
            if (!password.equals(repositoryResponse.getPassword())){
                hasUpdatedPassword = true;
                repositoryResponse.setPassword(password);
            }
        }

        boolean hasUpdatedPfp = false;
        if (dto.pfp().isPresent()){
            String pfpB64 = dto.pfp().get();
            if (!pfpB64.equals(repositoryResponse.getPfp())){
                hasUpdatedPfp = true;
                repositoryResponse.setPfp(pfpB64);
            }
        }

        repositoryResponse = repository.save(repositoryResponse);

        if (!repositoryResponse.equalsDto(dto) && !hasUpdatedPassword && !hasUpdatedPfp) {
            throw new DatabaseException("user-not-updated", "User couldn't be updated at the moment, try again later.");
        }

        log.info("{}.updateUser() >> user updated successfully", CLASS_PATH);
        return mapper.entityToResponseDTO(repositoryResponse);
    }

    public void deleteUser(UUID uuid) {
        log.info("{}.deleteUser() >> uuid :: {}", CLASS_PATH, uuid);

        repository.deleteById(uuid);

        if (repository.findById(uuid).isPresent()) {
            throw new DatabaseException("user-not-deleted", "User couldn't be deleted at the moment, try again later.");
        }
        log.info("{}.deleteUser() >> user deleted successfully", CLASS_PATH);
    }

    public List<GroupDTO> getUserGroups(UUID uuid) {
        log.info("{}.getUserGroups() >> uuid :: {}", CLASS_PATH, uuid);

        UserModel repositoryResponse = repository.findById(uuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));
        log.info("{}.getUserGroups() >> user found", CLASS_PATH);

        List<GroupDTO> userGroups = repositoryResponse.getGroups().stream().map(groupMapper::entityToDTO).toList();
        log.info("{}.getUserGroups() >> userGroups :: {}", CLASS_PATH, userGroups.stream().map(GroupDTO::name).toList());

        return userGroups;
    }

}
