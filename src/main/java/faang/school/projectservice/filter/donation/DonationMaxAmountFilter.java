package faang.school.projectservice.filter.donation;

import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.model.Donation;
import org.springframework.stereotype.Component;

@Component
public class DonationMaxAmountFilter extends DonationFilter {
    @Override
    public Object getFilterFieldValue(DonationFilterDto filters) {
        return filters.getMaxAmountPattern();
    }

    @Override
    public boolean apply(Donation donation, DonationFilterDto filters) {
        return donation.getAmount().floatValue() <= filters.getMaxAmountPattern().floatValue();
    }
}
