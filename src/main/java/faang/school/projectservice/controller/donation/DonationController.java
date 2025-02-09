package faang.school.projectservice.controller.donation;

import faang.school.projectservice.dto.donation.DonationDto;
import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.mapper.donation.DonationMapper;
import faang.school.projectservice.model.Donation;
import faang.school.projectservice.service.donation.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
public class DonationController {

    private final DonationService donationService;
    private final DonationMapper donationMapper;

    @PostMapping("/donation/create")
    public ResponseEntity<DonationDto> createDonation(@Valid @RequestBody DonationDto donationDtoRequest) {

        Donation donationRequest = donationMapper.toEntity(donationDtoRequest);

        Donation donationResponse = donationService.createDonation(donationRequest);

        DonationDto donationDtoResponse = donationMapper.toDto(donationResponse);

        return ResponseEntity.ok(donationDtoResponse);
    }

    @PostMapping("/donation/{donationId}")
    public ResponseEntity<DonationDto> getDonationById(@PathVariable long donationId) {

        Donation donationResponse = donationService.getDonationById(donationId);

        DonationDto donationDtoResponse = donationMapper.toDto(donationResponse);

        return ResponseEntity.ok(donationDtoResponse);
    }

    @PostMapping("/donations")
    public ResponseEntity<List<DonationDto>> getAllUserDonations(
            @RequestBody(required = false) DonationFilterDto dtoFilters) {

        List<Donation> donationsResponse = donationService.getAllUserDonations(dtoFilters);

        List<DonationDto> donationDtosResponse = donationMapper.toDto(donationsResponse);

        return ResponseEntity.ok(donationDtosResponse);
    }
}
