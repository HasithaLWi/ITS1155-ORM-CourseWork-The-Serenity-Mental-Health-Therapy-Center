package lk.ijse.theserenitymentalhealththerapycenter.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "patient_therapy_programs")
@Data
@NoArgsConstructor
@ToString
public class PatientTherapyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private TherapyProgram program;

    /**
     * Number of sessions paid upfront at registration time.
     */
    @Column(name = "upfront_sessions_paid")
    private int upfrontSessionsPaid = 0;

    /**
     * Number of sessions already created/scheduled using upfront credit.
     */
    @Column(name = "sessions_used")
    private int sessionsUsed = 0;

    /**
     * Get remaining upfront credit (sessions that can still be scheduled without payment).
     */
    public int getRemainingCredit() {
        return upfrontSessionsPaid - sessionsUsed;
    }

    public PatientTherapyProgram(Patient patient, TherapyProgram program, int upfrontSessionsPaid) {
        this.patient = patient;
        this.program = program;
        this.upfrontSessionsPaid = upfrontSessionsPaid;
        this.sessionsUsed = 0;
    }
}
