package neflo.dev.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.DatabaseException;
import neflo.dev.exceptions.NoEntitiesFoundException;
import neflo.dev.exceptions.ValidationException;
import neflo.dev.mapper.GroupMapper;
import neflo.dev.mapper.TripMapper;
import neflo.dev.mapper.UserMapper;
import neflo.dev.model.dto.TripDTO;
import neflo.dev.model.dto.group.GroupDTO;
import neflo.dev.model.dto.group.GroupMemberBalanceDTO;
import neflo.dev.model.dto.group.GroupMemberDTO;
import neflo.dev.model.dto.group.GroupRequestDTO;
import neflo.dev.model.dto.user.UserResponseDTO;
import neflo.dev.model.entity.GroupModel;
import neflo.dev.model.entity.TripModel;
import neflo.dev.model.entity.UserModel;
import neflo.dev.repository.GroupRepository;
import neflo.dev.repository.UserRepository;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class GroupService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;
    private static final String CLASS_PATH = "TripCount.GroupService";

    private final GroupRepository repository;
    private final UserRepository userRepository;
    private final GroupMapper mapper;
    private final UserMapper userMapper;
    private final TripMapper tripMapper;
    private final GroupCodeGenerator codeGenerator;

    public GroupDTO getGroupDetail(UUID userUuid, UUID groupUuid) {
        log.info("{}.getGroupDetail() >> groupUuid :: {}", CLASS_PATH, groupUuid);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));

        checkUserIsMember(userUuid, repositoryResponse);

        log.info("{}.getGroupDetail() >> group found :: {}", CLASS_PATH, repositoryResponse);
        return mapper.entityToDTO(repositoryResponse);
    }

    public GroupDTO updateGroup(UUID userUuid, UUID groupUuid, GroupDTO dto) {
        log.info("{}.updateGroup() >> groupUuid :: {} -- dto :: {}", CLASS_PATH, groupUuid, dto);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));

        checkUserIsMember(userUuid, repositoryResponse);

        mapper.updateEntity(repositoryResponse, dto);

        boolean hasUpdatedPfp = false;
        if (dto.pfp().isPresent()){
            String pfpB64 = dto.pfp().get();
            if (!pfpB64.equals(repositoryResponse.getPfp())){
                hasUpdatedPfp = true;
                repositoryResponse.setPfp(pfpB64);
            }
        }

        repositoryResponse = repository.save(repositoryResponse);

        if (!repositoryResponse.equalsDto(dto) && !hasUpdatedPfp) {
            throw new DatabaseException("group-not-updated", "Group couldn't be updated at the moment, try again later.");
        }

        log.info("{}.updateGroup() >> group updated successfully", CLASS_PATH);
        return mapper.entityToDTO(repositoryResponse);
    }

    public void leaveGroup(UUID userUuid, UUID groupUuid) {
        log.info("{}.leaveGroup() >> groupUuid :: {} -- userUuid :: {}", CLASS_PATH, groupUuid, userUuid);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));

        if (repositoryResponse.getMembers().size() == 1 && repositoryResponse.getMembers().get(0).getId().equals(userUuid)){
            repositoryResponse.getMembers().forEach(
                    user -> user.getGroups().remove(repositoryResponse)
            );
            repository.delete(repositoryResponse);
            log.info("{}.leaveGroup() >> group was deleted since there are no users related to it :: {}", CLASS_PATH, groupUuid);
            return;
        }

        boolean wasRemoved = repositoryResponse.getMembers().removeIf(m -> m.getId().equals(userUuid));

        if (wasRemoved) repository.save(repositoryResponse);
    }

    public List<GroupMemberBalanceDTO> getGroupMembersBalance(UUID userUuid, UUID groupUuid) {
        log.info("{}.getGroupMembersBalance() >> groupUuid :: {}", CLASS_PATH, groupUuid);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));
        log.info("{}.getGroupMembersBalance() >> group found", CLASS_PATH);

        checkUserIsMember(userUuid, repositoryResponse);

        List<GroupMemberBalanceDTO> groupMembers = getGroupMemberBalanceList(repositoryResponse);
        log.info("{}.getGroupMembersBalance() >> groupMembers :: {}", CLASS_PATH, groupMembers.stream().map(GroupMemberBalanceDTO::nickname).toList());

        return groupMembers;
    }

    private List<GroupMemberBalanceDTO> getGroupMemberBalanceList(GroupModel groupModel) {
        List<UserModel> members = groupModel.getMembers();
        List<TripModel> trips = groupModel.getTrips();

        int totalDuration = trips.stream()
                .mapToInt(TripModel::getDurationMinutes)
                .sum();

        Map<UUID, Integer> driverDurations = trips.stream()
                .collect(Collectors.groupingBy(
                        trip -> trip.getDriver().getId(),
                        Collectors.summingInt(TripModel::getDurationMinutes)
                ));

        return members.stream()
                .map(member -> {
                    int driverDuration = driverDurations.getOrDefault(member.getId(), 0);
                    int balance = 2 * driverDuration - totalDuration;
                    return new GroupMemberBalanceDTO(member.getNickname(), balance);
                }).toList();
    }

    public List<GroupMemberDTO> getGroupMembers(UUID userUuid, UUID groupUuid) {
        log.info("{}.getGroupMembers() >> groupUuid :: {}", CLASS_PATH, groupUuid);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));
        log.info("{}.getGroupMembers() >> group found", CLASS_PATH);

        checkUserIsMember(userUuid, repositoryResponse);

        List<GroupMemberDTO> groupMembers = repositoryResponse.getMembers().stream().map(userMapper::entityToGroupMemberDTO).toList();
        log.info("{}.getGroupMembers() >> groupMembers :: {}", CLASS_PATH, groupMembers.stream().map(GroupMemberDTO::nickname).toList());

        return groupMembers;
    }

    public List<TripDTO> getGroupTrips(UUID userUuid, UUID groupUuid) {
        log.info("{}.getGroupTrips() >> groupUuid :: {}", CLASS_PATH, groupUuid);

        GroupModel repositoryResponse = repository.findById(groupUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found."));
        log.info("{}.getGroupTrips() >> group found", CLASS_PATH);

        checkUserIsMember(userUuid, repositoryResponse);

        List<TripDTO> groupMembers = repositoryResponse.getTrips().stream().map(tripMapper::entityToDTO).toList();
        log.info("{}.getGroupTrips() >> groupMembers :: {}", CLASS_PATH, groupMembers.stream().map(TripDTO::date).toList());

        return groupMembers;
    }

    private static void checkUserIsMember(UUID userUuid, GroupModel repositoryResponse) {
        if (repositoryResponse.getMembers().stream().noneMatch(m -> m.getId().equals(userUuid))) {
            throw new ValidationException("non-member-access", "Non members cannot access a group's data");
        }
    }

    @Transactional
    public GroupDTO createGroup(UUID userUuid, GroupRequestDTO groupDTO) {
        UserModel foundUser = userRepository.findById(userUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));

        for (int attempt = 1; attempt <= MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            GroupModel groupModel = GroupModel.builder()
                    .name(groupDTO.name())
                    .groupCode(codeGenerator.generate())
                    .build();

            if (groupDTO.pfp().isPresent()){
                groupModel.setPfp(groupDTO.pfp().get());
            }

            groupModel.addMember(foundUser);

            try {
                return mapper.entityToDTO(repository.save(groupModel));
            } catch (DataIntegrityViolationException e) {
                if (!isGroupCodeCollision(e)) {
                    throw new DatabaseException("group-saving-error", "Unable to save new group.");
                }

                log.warn("{}.createGroupModel() >> Group code collision on attempt {}", CLASS_PATH, attempt);
            }
        }

        throw new DatabaseException("group-code-not-generated", "Unable to generate a unique group  after " + MAX_CODE_GENERATION_ATTEMPTS + " attempts.");
    }

    private boolean isGroupCodeCollision(DataIntegrityViolationException exception) {
        Throwable cause = exception;

        while (cause != null) {
            if (cause instanceof PSQLException psqlException) {
                ServerErrorMessage error = psqlException.getServerErrorMessage();

                return error != null && "UK_GRP_GROUP_CODE".equals(error.getConstraint());
            }

            cause = cause.getCause();
        }

        return false;
    }

}
