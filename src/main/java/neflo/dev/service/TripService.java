package neflo.dev.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neflo.dev.exceptions.DatabaseException;
import neflo.dev.exceptions.NoEntitiesFoundException;
import neflo.dev.exceptions.ValidationException;
import neflo.dev.mapper.TripMapper;
import neflo.dev.model.dto.trip.TripCreateDTO;
import neflo.dev.model.dto.trip.TripDTO;
import neflo.dev.model.entity.GroupModel;
import neflo.dev.model.entity.TripModel;
import neflo.dev.model.entity.UserModel;
import neflo.dev.repository.GroupRepository;
import neflo.dev.repository.TripRepository;
import neflo.dev.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class TripService {

    private static final String CLASS_PATH = "TripCount.TripService";

    private final TripRepository repository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TripMapper mapper;

    public TripDTO createTrip(UUID userUuid, UUID groupUuid, TripCreateDTO dto) {
        log.info("{}.createTrip() >> tripDto :: {}", CLASS_PATH, dto);
        UserModel foundUser = userRepository.findById(userUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));
        log.info("{}.createTrip() >> user found", CLASS_PATH);

        GroupModel foundGroup = groupRepository.findByIdAndMembers_IdIn(groupUuid, List.of(dto.driver()))
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found with selected member."));
        log.info("{}.createTrip() >> group found", CLASS_PATH);

        if (foundGroup.getMembers().stream().noneMatch(m -> m.getId().equals(foundUser.getId()))){
            throw new ValidationException("non-member-access", "Non members cannot create a trip in an external group");
        }

        UserModel driver = foundGroup.getMembers().stream().filter(m -> m.getId().equals(dto.driver())).findFirst().orElse(null);
        if (foundGroup.getMembers().stream().noneMatch(m -> m.getId().equals(foundUser.getId()))){
            throw new ValidationException("invalid-driver", "Selected driver is not valid");
        }

        TripModel trip = TripModel.builder()
                .driver(driver)
                .group(foundGroup)
                .date(dto.date())
                .durationMinutes(dto.durationMinutes())
                .origin(dto.origin())
                .destination(dto.destination())
                .notes(dto.notes())
                .build();

        TripDTO response = mapper.entityToDTO(repository.save(trip));
        log.info("{}.createTrip() >> trip created :: {}", CLASS_PATH, response);

        return response;
    }

    public TripDTO updateTrip(UUID userUuid, UUID groupUuid, UUID tripUuid, TripCreateDTO dto) {
        log.info("{}.updateTrip() >> tripUuid :: {} -- dto :: {}", CLASS_PATH, tripUuid, dto);

        TripModel repositoryResponse = repository.findById(tripUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("trip-not-found", "Trip not found."));

        checkValidRequest(userUuid, groupUuid, repositoryResponse);

        mapper.updateEntity(repositoryResponse, dto);
        boolean hasUpdatedOrigin = false;
        if (dto.origin() != null){
            if (!dto.origin().equals(repositoryResponse.getOrigin())){
                hasUpdatedOrigin = true;
                repositoryResponse.setOrigin(dto.origin());
            }
        }
        boolean hasUpdatedDestination = false;
        if (dto.destination() != null){
            if (!dto.destination().equals(repositoryResponse.getDestination())){
                hasUpdatedDestination = true;
                repositoryResponse.setDestination(dto.destination());
            }
        }
        boolean hasUpdatedNotes = false;
        if (dto.notes() != null){
            if (!dto.notes().equals(repositoryResponse.getNotes())){
                hasUpdatedNotes = true;
                repositoryResponse.setNotes(dto.notes());
            }
        }

        Optional<UserModel> driver = userRepository.findById(dto.driver());
        if (driver.isPresent()) {
            repositoryResponse.setDriver(driver.get());
        }

        repositoryResponse = repository.save(repositoryResponse);

        if (!repositoryResponse.equalsDto(dto) && !hasUpdatedOrigin && !hasUpdatedDestination && hasUpdatedNotes) {
            throw new DatabaseException("trip-not-updated", "Trip couldn't be updated at the moment, try again later.");
        }

        log.info("{}.updateTrip() >> trip updated successfully", CLASS_PATH);
        return mapper.entityToDTO(repositoryResponse);
    }

    public void deleteTrip(UUID userUuid, UUID groupUuid, UUID tripUuid) {
        log.info("{}.deleteTrip() >> tripUuid :: {}", CLASS_PATH, tripUuid);

        TripModel repositoryResponse = repository.findById(tripUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("trip-not-found", "Trip not found."));

        checkValidRequest(userUuid, groupUuid, repositoryResponse);

        repository.delete(repositoryResponse);
    }

    public TripDTO getTripDetail(UUID userUuid, UUID groupUuid, UUID tripUuid) {
        log.info("{}.getTripDetail() >> tripUuid :: {}", CLASS_PATH, tripUuid);

        TripModel repositoryResponse = repository.findById(tripUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("trip-not-found", "Trip not found."));

        checkValidRequest(userUuid, groupUuid, repositoryResponse);

        log.info("{}.getTripDetail() >> trip found :: {}", CLASS_PATH, repositoryResponse);
        return mapper.entityToDTO(repositoryResponse);
    }

    private static void checkValidRequest(UUID userUuid, UUID groupUuid, TripModel repositoryResponse) {
        if (!repositoryResponse.getGroup().getId().equals(groupUuid)) {
            throw new ValidationException("non-group-access", "Non related groups cannot access external trips");
        }

        if (repositoryResponse.getGroup().getMembers().stream().noneMatch(m -> m.getId().equals(userUuid))) {
            throw new ValidationException("non-member-access", "Non members cannot access a group's trip data");
        }
    }


}
