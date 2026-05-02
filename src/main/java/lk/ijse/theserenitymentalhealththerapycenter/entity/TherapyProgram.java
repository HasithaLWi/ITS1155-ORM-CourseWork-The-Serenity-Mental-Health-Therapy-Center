package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "therapy_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TherapyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String duration;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(length = 500)
    private String description;

//    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @ToString.Exclude
//    private List<PatientTherapyProgram> patientTherapyPrograms = new ArrayList<>();

    @ManyToMany(mappedBy = "programs")
    @ToString.Exclude
    private List<Patient> patients = new ArrayList<>();

    @ManyToMany(mappedBy = "programs")
    @ToString.Exclude
    private Set<Therapist> therapists = new HashSet<>();
}
