package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;
import org.springframework.stereotype.Component;

@Component
public class MomentAfterDateFilter extends MomentFilter {

    @Override
    public Object getFilterFieldValue(MomentFilterDto filters) {
        return filters.getAfterDatePattern();
    }

    @Override
    public boolean apply(Moment moment, MomentFilterDto filters) {
        return moment.getDate().isAfter(filters.getAfterDatePattern());
    }
}
