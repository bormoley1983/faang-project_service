package faang.school.projectservice.mapper.donation;

import faang.school.projectservice.dto.donation.DonationDto;
import faang.school.projectservice.model.Donation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DonationMapper {

    @Mapping(source = "campaign.id", target = "campaignId")
    DonationDto toDto(Donation donation);

    @Mapping(target = "campaign", expression = "java(Campaign.builder().id(donationDto.getCampaignId()).build())")
    Donation toEntity(DonationDto donationDto);

    List<DonationDto> toDto(List<Donation> donations);

    List<Donation> toEntity(List<DonationDto> donationDtos);
}
