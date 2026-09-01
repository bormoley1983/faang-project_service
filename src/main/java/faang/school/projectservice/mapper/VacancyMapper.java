package faang.school.projectservice.mapper;

import faang.school.projectservice.dto.project.ProjectDto;
import faang.school.projectservice.dto.vacancy.VacancyCreateDto;
import faang.school.projectservice.dto.vacancy.VacancyDto;
import faang.school.projectservice.dto.vacancy.VacancyUpdateDto;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.model.Vacancy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VacancyMapper {

    @BeanMapping(ignoreByDefault = true)
    Vacancy toEntity(VacancyCreateDto dto);

    @Mapping(target = "project", source = "project")
    VacancyDto toDto(Vacancy dto);

    ProjectDto toProjectDto(Project project);

    @BeanMapping(ignoreByDefault = true)
    Vacancy toEntity(VacancyUpdateDto dto);

    List<VacancyDto> toDto(List<Vacancy> vacancies);
}
