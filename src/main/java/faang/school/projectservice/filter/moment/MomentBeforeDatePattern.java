package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class MomentBeforeDatePattern extends MomentFilter {
    @Override
    public Object getFilterFieldValue(MomentFilterDto filters) {
        return filters.getBeforeDatePattern();
    }

    @Override
    public boolean apply(Moment moment, MomentFilterDto filters) {
        return moment.getDate().isBefore(filters.getBeforeDatePattern());
    }
}
