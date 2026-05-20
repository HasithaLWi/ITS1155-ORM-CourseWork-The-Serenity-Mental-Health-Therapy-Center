package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", length = 20)
    private PaymentType paymentType = PaymentType.SINGLE;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(length = 300)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<TherapySession> coveredSessions = new ArrayList<>();

    public enum PaymentMethod {
        CASH, CARD, BANK_TRANSFER, INSURANCE
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, REFUNDED, FAILED
    }

    public enum PaymentType {
        SINGLE, UPFRONT, PARTIAL_UPFRONT, EXPENSE
    }
}
