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
import org.checkerframework.checker.units.qual.A;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class TripService {

    private static final String CLASS_PATH = "TripCount.TripService";

    private final TripRepository repository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TripMapper mapper;

    private static void checkValidRequest(UUID userUuid, UUID groupUuid, TripModel repositoryResponse) {
        if (!repositoryResponse.getGroup().getId().equals(groupUuid)) {
            throw new ValidationException("non-group-access", "Non related groups cannot access external trips");
        }

        if (repositoryResponse.getGroup().getMembers().stream().noneMatch(m -> m.getId().equals(userUuid))) {
            throw new ValidationException("non-member-access", "Non members cannot access a group's trip data");
        }
    }

    public TripDTO createTrip(UUID userUuid, UUID groupUuid, TripCreateDTO dto) {
        log.info("{}.createTrip() >> tripDto :: {}", CLASS_PATH, dto);
        UserModel foundUser = getUserOrThrow(userUuid);
        log.info("{}.createTrip() >> user found", CLASS_PATH);

        GroupModel foundGroup = groupRepository.findByIdAndMembers_IdIn(groupUuid, List.of(dto.driver()))
                .orElseThrow(() -> new NoEntitiesFoundException("group-not-found", "Group not found with selected member."));
        log.info("{}.createTrip() >> group found", CLASS_PATH);

        List<UserModel> passengers = getPassengersOrThrow(dto.passengers(), foundGroup);

        if (foundGroup.getMembers().stream().noneMatch(m -> m.getId().equals(foundUser.getId()))) {
            throw new ValidationException("non-member-access", "Non members cannot create a trip in an external group");
        }

        UserModel driver = foundGroup.getMembers().stream().filter(m -> m.getId().equals(dto.driver())).findFirst().orElse(null);
        if (foundGroup.getMembers().stream().noneMatch(m -> m.getId().equals(foundUser.getId()))) {
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
                .passengers(passengers)
                .build();

        TripDTO response = mapper.entityToDTO(repository.save(trip));
        log.info("{}.createTrip() >> trip created :: {}", CLASS_PATH, response);

        return response;
    }

    private @NonNull List<UserModel> getPassengersOrThrow(List<UUID> passengersUuids, GroupModel foundGroup) {
        List<UserModel> passengers = passengersUuids.stream()
                .distinct()
                .map(this::getUserOrThrow)
                .toList();

        for (UserModel passenger : passengers) {
            if (passenger.getGroups().stream().noneMatch(g -> g.getId().equals(foundGroup.getId()))) {
                throw new ValidationException("passenger-not-member", "All of the passengers should be members of the trip's group.");
            }
        }

        return passengers;
    }

    private @NonNull UserModel getUserOrThrow(UUID userUuid) {
        UserModel foundUser = userRepository.findById(userUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("user-not-found", "User not found."));
        return foundUser;
    }

    public TripDTO updateTrip(UUID userUuid, UUID groupUuid, UUID tripUuid, TripCreateDTO dto) {
        log.info("{}.updateTrip() >> tripUuid :: {} -- dto :: {}", CLASS_PATH, tripUuid, dto);

        TripModel repositoryResponse = repository.findById(tripUuid)
                .orElseThrow(() -> new NoEntitiesFoundException("trip-not-found", "Trip not found."));

        checkValidRequest(userUuid, groupUuid, repositoryResponse);

        List<UserModel> passengers = getPassengersOrThrow(dto.passengers(), repositoryResponse.getGroup());

        mapper.updateEntity(repositoryResponse, dto);
        int updateCount = 0;

        if (dto.origin() != null) {
            if (!dto.origin().equals(repositoryResponse.getOrigin())) {
                updateCount++;
                repositoryResponse.setOrigin(dto.origin());
            }
        }

        if (dto.destination() != null) {
            if (!dto.destination().equals(repositoryResponse.getDestination())) {
                updateCount++;
                repositoryResponse.setDestination(dto.destination());
            }
        }

        if (dto.notes() != null) {
            if (!dto.notes().equals(repositoryResponse.getNotes())) {
                updateCount++;
                repositoryResponse.setNotes(dto.notes());
            }
        }

        if (!passengers.isEmpty()) {
            if (repositoryResponse.getPassengers().isEmpty() || !repositoryResponse.getPassengers().stream()
                    .map(p -> p.getId().toString()).toList().equals(passengers.stream().map(p -> p.getId().toString()).toList())) {
                updateCount++;
                repositoryResponse.setPassengers(new ArrayList<>(passengers));
            }
        }

        Optional<UserModel> driver = userRepository.findById(dto.driver());
        if (driver.isPresent()) {
            repositoryResponse.setDriver(driver.get());
        }

        repositoryResponse = repository.save(repositoryResponse);

        if (!repositoryResponse.equalsDto(dto) && updateCount == 0) {
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


}
