package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lk.ijse.theserenitymentalhealththerapycenter.enumaration.TherapistScheduleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "therapist_availability")
public class TherapistAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDate date;
    private String time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;
}
