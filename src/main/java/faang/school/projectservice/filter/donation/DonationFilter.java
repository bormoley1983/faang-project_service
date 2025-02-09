package faang.school.projectservice.filter.donation;

import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.model.Donation;

public abstract class DonationFilter {
    public boolean isApplicable(DonationFilterDto filters) {
        return filters != null && getFilterFieldValue(filters) != null;
    }

    public abstract Object getFilterFieldValue(DonationFilterDto filters);

    public abstract boolean apply(Donation donation, DonationFilterDto filters);
}
