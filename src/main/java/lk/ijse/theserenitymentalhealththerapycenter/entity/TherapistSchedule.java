package lk.ijse.theserenitymentalhealththerapycenter.entity;

import jakarta.persistence.*;
import lk.ijse.theserenitymentalhealththerapycenter.enumaration.TherapistScheduleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class TherapistSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Enumerated(EnumType.STRING)
    private TherapistScheduleTypes scheduleType;
    private LocalDate date;
    private String time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "therapist_id", nullable = false)
    private Therapist therapist;
}
