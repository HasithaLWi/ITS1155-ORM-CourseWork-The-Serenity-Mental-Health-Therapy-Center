package lk.ijse.theserenitymentalhealththerapycenter.dto;


import lk.ijse.theserenitymentalhealththerapycenter.enumaration.TherapistScheduleTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TherapistScheduleDTO {
    @EqualsAndHashCode.Include
    private long id;
    private TherapistScheduleTypes scheduleType;
    private LocalDate date;
    private String time;

    private long therapistId;
}
