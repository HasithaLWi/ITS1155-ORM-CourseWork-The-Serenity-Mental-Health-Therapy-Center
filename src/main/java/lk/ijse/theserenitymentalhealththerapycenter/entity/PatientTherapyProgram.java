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


    @Column(name = "sessions_paid")
    private int sessionsPaid = 0;


    @Column(name = "sessions_used")
    private int sessionsUsed = 0;

    public int getRemainingCredit() {
        return sessionsPaid - sessionsUsed;
    }

    public PatientTherapyProgram(Patient patient, TherapyProgram program, int sessionsPaid) {
        this.patient = patient;
        this.program = program;
        this.sessionsPaid = sessionsPaid;
        this.sessionsUsed = 0;
    }
}
