package neflo.dev.mapper;

import neflo.dev.model.dto.TripDTO;
import neflo.dev.model.entity.TripModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "driver", source = "driver.nickname")
    TripDTO entityToDTO(TripModel entity);

}
