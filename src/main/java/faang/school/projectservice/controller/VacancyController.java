package faang.school.projectservice.controller;

import faang.school.projectservice.dto.CandidateAddDto;
import faang.school.projectservice.config.context.user.UserContext;
import faang.school.projectservice.dto.vacancy.FilterVacancyDto;
import faang.school.projectservice.dto.vacancy.VacancyCreateDto;
import faang.school.projectservice.dto.vacancy.VacancyDto;
import faang.school.projectservice.dto.vacancy.VacancyUpdateDto;
import faang.school.projectservice.mapper.VacancyMapper;
import faang.school.projectservice.model.Vacancy;
import faang.school.projectservice.service.VacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@RequestMapping("/api/v1/vacancy")
@RestController
public class VacancyController {
    private final VacancyService vacancyService;
    private final VacancyMapper vacancyMapper;
    private final UserContext userContext;

    @PostMapping
    public ResponseEntity<VacancyDto> createVacancy(@Valid @RequestBody VacancyCreateDto vacancyCreateDto) {
        Vacancy vacancy = vacancyMapper.toEntity(vacancyCreateDto);
        Vacancy vacancyResponse= vacancyService.createVacancy(vacancy, userContext.getUserId(),vacancyCreateDto.getProjectId());
        return ResponseEntity.status(HttpStatus.CREATED).body(vacancyMapper.toDto(vacancyResponse));
    }

    @PutMapping("/{vacancyId}")
    public ResponseEntity<VacancyDto> updateVacancy(@Valid @RequestBody VacancyUpdateDto vacancyUpdateDto,
                              @PathVariable Long vacancyId) {
        Vacancy vacancy = vacancyMapper.toEntity(vacancyUpdateDto);
        Vacancy vacancyResponse= vacancyService.updateVacancy(vacancy, vacancyId, userContext.getUserId());
        return ResponseEntity.ok(vacancyMapper.toDto(vacancyResponse));
    }

    @PostMapping("/{vacancyId}/candidates")
    public ResponseEntity<Void> addCandidate(@Valid @RequestBody CandidateAddDto candidateAddDto,
                             @PathVariable Long vacancyId) {
        vacancyService.addCandidatesToVacancy(candidateAddDto.getCandidatesIds(),
                candidateAddDto.getProjectId(), vacancyId, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{vacancyId}")
    public ResponseEntity<Void> removeVacancy(@PathVariable Long vacancyId) {
        vacancyService.removeVacancy(vacancyId, userContext.getUserId());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    public ResponseEntity<Page<VacancyDto>> getFilteredByNameAndPositionVacancies(
            FilterVacancyDto filterVacancyDto, Pageable pageable) {
        Page<VacancyDto> vacancies = vacancyService.filterVacancies(filterVacancyDto.getPosition(),
                filterVacancyDto.getVacancyName(), pageable).map(vacancyMapper::toDto);
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/{vacancyId}")
    public ResponseEntity<VacancyDto> findVacancyById(@PathVariable Long vacancyId) {
        Vacancy vacancy = vacancyService.getVacancyById(vacancyId);
        return ResponseEntity.ok(vacancyMapper.toDto(vacancy));
    }
}
