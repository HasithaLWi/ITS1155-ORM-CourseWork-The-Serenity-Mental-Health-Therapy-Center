package lk.ijse.theserenitymentalhealththerapycenter.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
//
//@Entity
//@Table(name = "patient_program")
@Data
@NoArgsConstructor
@ToString
public class PatientTherapyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private TherapyProgram program;
}
