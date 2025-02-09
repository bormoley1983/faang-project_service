package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class MomentDescriptionFilter extends MomentFilter {
    @Override
    public Object getFilterFieldValue(MomentFilterDto filters) {
        return filters.getDescriptionPattern();
    }

    @Override
    public boolean apply(Moment moment, MomentFilterDto filters) {
        return StringUtils.contains(moment.getDescription(),
                filters.getDescriptionPattern());
    }
}
