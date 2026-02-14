package faang.school.projectservice.filter.moment;

import faang.school.projectservice.dto.moment.MomentFilterDto;
import faang.school.projectservice.model.Moment;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;

@Component
public class MomentNameFilter extends MomentFilter {
    @Override
    public Object getFilterFieldValue(MomentFilterDto filters) {
        return filters.getNamePattern();
    }

    @Override
    public boolean apply(Moment moment, MomentFilterDto filters) {
        return Strings.CS.contains(moment.getName(), filters.getNamePattern());
    }
}
