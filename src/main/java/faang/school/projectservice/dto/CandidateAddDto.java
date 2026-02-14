package faang.school.projectservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CandidateAddDto {
    @NotEmpty
    private List<Long> candidatesIds;
    @NotNull
    private Long projectId;
    @NotNull
    private Long vacancyId;
}
