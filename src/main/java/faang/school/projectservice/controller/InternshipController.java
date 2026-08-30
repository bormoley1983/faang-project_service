package faang.school.projectservice.controller;

import faang.school.projectservice.dto.project.InternshipDto;
import faang.school.projectservice.mapper.InternshipMapper;
import faang.school.projectservice.model.Internship;
import faang.school.projectservice.model.InternshipStatus;
import faang.school.projectservice.service.InternshipService;
import faang.school.projectservice.validator.InternshipValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@RequestMapping("/api/v1/internships")
@RestController
public class InternshipController {

    private final InternshipService internshipService;
    private final InternshipMapper internshipMapper;
    private final InternshipValidator internshipValidator;

    @PostMapping
    public InternshipDto createInternship(@Valid @RequestBody InternshipDto internshipDto) {
        internshipValidator.validateForCreation(internshipDto);
        Internship internship = internshipMapper.toEntity(internshipDto);
        Internship savedInternship = internshipService.createInternship(internship);
        return internshipMapper.toDto(savedInternship);
    }

    @PatchMapping("/{id}")
    public InternshipDto partialUpdateInternship(
            @PathVariable Long id,
            @Valid @RequestBody InternshipDto internshipDto) {
        internshipValidator.validatePartialUpdate(internshipDto);
        Internship updatedInternship = internshipService.partialUpdateInternship(id, internshipDto);
        return internshipMapper.toDto(updatedInternship);
    }

    @GetMapping("/{id}")
    public InternshipDto getInternshipById(@PathVariable @Positive Long id) {
        Internship internship = internshipService.getInternshipById(id);
        return internshipMapper.toDto(internship);
    }

    @GetMapping
    public Page<InternshipDto> getInternships(
            @RequestParam(required = false) InternshipStatus status,
            @RequestParam(required = false) Long roleId,
            Pageable pageable
    ) {
        return internshipService.getInternships(status, roleId, pageable).map(internshipMapper::toDto);
    }
}
