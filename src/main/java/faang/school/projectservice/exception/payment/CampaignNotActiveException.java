package faang.school.projectservice.exception.payment;

public class CampaignNotActiveException extends RuntimeException {
    public CampaignNotActiveException(String message) {
        super(message);
    }
}
