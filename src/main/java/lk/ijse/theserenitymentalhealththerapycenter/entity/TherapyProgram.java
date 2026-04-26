package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.HashSet;
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

    @ManyToMany(mappedBy = "programs")
    @ToString.Exclude
    private Set<Therapist> therapists = new HashSet<>();

    @ManyToMany(mappedBy = "programs")
    @ToString.Exclude
    private Set<Patient> patients = new HashSet<>();
}
