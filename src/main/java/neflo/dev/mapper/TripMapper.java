package neflo.dev.mapper;

import neflo.dev.model.dto.trip.TripCreateDTO;
import neflo.dev.model.dto.trip.TripDTO;
import neflo.dev.model.dto.trip.TripRequestDTO;
import neflo.dev.model.entity.TripModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "driver", source = "driver.nickname")
    TripRequestDTO entityToRequestDTO(TripModel entity);

    @Mapping(target = "driver", source = "driver.nickname")
    @Mapping(target = "driverId", source = "driver.id")
    TripDTO entityToDTO(TripModel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "driver", ignore = true)
    void updateEntity(@MappingTarget TripModel entity, TripCreateDTO dto);

}
