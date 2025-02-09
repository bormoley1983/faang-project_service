package faang.school.projectservice.dto.moment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MomentFilterDto {
    private String namePattern;
    private String descriptionPattern;
    private List<Long> projectIdsPattern;
    private LocalDateTime afterDatePattern;
    private LocalDateTime beforeDatePattern;
}
