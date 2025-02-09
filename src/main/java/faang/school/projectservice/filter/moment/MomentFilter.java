package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;

public abstract class MomentFilter {
    public boolean isApplicable(MomentFilterDto filters) {
        return filters != null && getFilterFieldValue(filters) != null;
    }

    public abstract Object getFilterFieldValue(MomentFilterDto filters);

    public abstract boolean apply(Moment moment, MomentFilterDto filters);
}