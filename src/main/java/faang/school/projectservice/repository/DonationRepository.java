package faang.school.projectservice.repository;

import faang.school.projectservice.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.math.BigDecimal;
import faang.school.projectservice.dto.client.Currency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByIdAndUserId(Long id, Long userId);

    List<Donation> findAllByUserId(Long userId);

    @Query("""
            SELECT d FROM Donation d
            WHERE d.userId = :userId
              AND (:date IS NULL OR CAST(d.donationTime AS LocalDate) = :date)
              AND (:currency IS NULL OR d.currency = :currency)
              AND (:minAmount IS NULL OR d.amount >= :minAmount)
              AND (:maxAmount IS NULL OR d.amount <= :maxAmount)
            """)
    Page<Donation> searchByUserId(Long userId, LocalDate date, Currency currency,
                                  BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    Optional<Donation> findByIdempotencyKey(UUID idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Donation d WHERE d.id = :id")
    Optional<Donation> findByIdForUpdate(Long id);
}
