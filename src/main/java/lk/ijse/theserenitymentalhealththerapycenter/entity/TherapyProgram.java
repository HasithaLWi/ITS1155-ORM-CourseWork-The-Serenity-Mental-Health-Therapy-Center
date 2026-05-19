package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TherapyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 50)
    private String duration;

    @Column(precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "total_sessions")
    private Integer totalSessions;

    @Column(name = "session_fee", precision = 10, scale = 2)
    private BigDecimal sessionFee;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "program", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<PatientTherapyProgram> patientTherapyPrograms = new ArrayList<>();

    @ManyToMany(mappedBy = "programs",  fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Therapist> therapists = new ArrayList<>();
}
