package faang.school.projectservice.service;

import faang.school.projectservice.dto.project.InternshipDto;
import faang.school.projectservice.model.Internship;
import faang.school.projectservice.model.InternshipStatus;
import faang.school.projectservice.repository.InternshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class InternshipService {

    private final InternshipRepository internshipRepository;

    @Transactional
    public Internship createInternship(Internship internship) {
        return internshipRepository.save(internship);
    }

    @Transactional
    public Internship updateInternship(Internship internship) {
        Internship existingInternship = internshipRepository.findById(internship.getId())
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));

        if (existingInternship.getStatus() == InternshipStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed internship.");
        }

        existingInternship.setStatus(internship.getStatus());
        return internshipRepository.save(existingInternship);
    }

    @Transactional
    public Internship partialUpdateInternship(Long id, InternshipDto internshipDto) {
        Internship existingInternship = internshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));

        Optional.ofNullable(internshipDto.getName()).ifPresent(existingInternship::setName);
        Optional.ofNullable(internshipDto.getDescription()).ifPresent(existingInternship::setDescription);
        Optional.ofNullable(internshipDto.getStartDate()).ifPresent(existingInternship::setStartDate);
        Optional.ofNullable(internshipDto.getEndDate()).ifPresent(existingInternship::setEndDate);
        Optional.ofNullable(internshipDto.getStatus()).ifPresent(existingInternship::setStatus);

        return internshipRepository.save(existingInternship);
    }

    @Transactional(readOnly = true)
    public Internship getInternshipById(Long id) {
        return internshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Internship not found"));
    }

    @Transactional(readOnly = true)
    public Page<Internship> getInternships(InternshipStatus status, Long roleId, Pageable pageable) {
        return internshipRepository.search(status, roleId, pageable);
    }
}
