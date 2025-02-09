package faang.school.projectservice.filter.donation;

import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.model.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMinAmountFilter extends DonationFilter {
    @Override
    public Object getFilterFieldValue(DonationFilterDto filters) {
        return filters.getMinAmountPattern();
    }

    @Override
    public boolean apply(Donation donation, DonationFilterDto filters) {
        return donation.getAmount().floatValue() >= filters.getMinAmountPattern().floatValue();
    }
}
