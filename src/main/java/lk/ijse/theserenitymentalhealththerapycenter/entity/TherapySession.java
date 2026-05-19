package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "therapy_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TherapySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "sequence_number")
    private Integer sequenceNumber;

    @Column(name = "session_date")
    private LocalDate sessionDate;

    @Column(name = "session_time")
    private LocalTime sessionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.UNSCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(length = 500)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "therapist_id")
    private Therapist therapist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private TherapyProgram program;

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upfront_payment_id")
    private Payment upfrontPayment;

    public enum SessionStatus {
        UNSCHEDULED, SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum PaymentStatus {
        PENDING, PAID
    }
}
