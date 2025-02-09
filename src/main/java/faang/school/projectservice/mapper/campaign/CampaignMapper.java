package faang.school.projectservice.mapper.campaign;

import faang.school.projectservice.dto.campaign.CampaignDto;
import faang.school.projectservice.model.Campaign;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CampaignMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "createdBy", target = "idCreatedBy")
    @Mapping(source = "updatedBy", target = "idUpdatedBy")
    @Mapping(source = "goal", target = "goalAmount")
    CampaignDto toDto(Campaign entity);

    @InheritInverseConfiguration
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Campaign toEntity(CampaignDto dto);

    List<CampaignDto> toDtoList(List<Campaign> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(source = "idUpdatedBy", target = "updatedBy")
    @Mapping(source = "goalAmount", target = "goal")
    Campaign updateCampaignFromDto(CampaignDto dto, @MappingTarget Campaign entity);
}
