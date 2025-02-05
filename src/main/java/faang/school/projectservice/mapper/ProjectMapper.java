package faang.school.projectservice.mapper;

import faang.school.projectservice.dto.project.CreateProjectRequestDto;
import faang.school.projectservice.dto.project.CreateSubProjectDto;
import faang.school.projectservice.dto.project.ProjectResponseDto;
import faang.school.projectservice.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "parentProjectId", source = "parentProject.id")
    @Mapping(target = "childrenIds", source = "children", qualifiedByName = "mapChildrenToIds")
    ProjectResponseDto toDto(Project project);

    Project toEntity(CreateProjectRequestDto createProjectRequestDto);

    Project toEntity(CreateSubProjectDto createSubProjectDto);

    @Named("mapChildrenToIds")
    default List<Long> mapChildrenToIds(List<Project> children) {
        return children == null || children.isEmpty()
                ? List.of()
                : children.stream().map(Project::getId).toList();
    }
}