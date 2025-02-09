package faang.school.projectservice.filter.donation;

import faang.school.projectservice.dto.donation.DonationFilterDto;
import faang.school.projectservice.model.Donation;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DonationDateFilter extends DonationFilter {

    @Override
    public Object getFilterFieldValue(DonationFilterDto filters) {
        return filters.getDatePattern();
    }

    @Override
    public boolean apply(Donation donation, DonationFilterDto filters) {
        LocalDate donationDate = donation.getDonationTime().toLocalDate();
        return donationDate.equals(filters.getDatePattern());
    }
}
