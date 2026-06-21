package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TherapistAvailabilityDTO {

    @EqualsAndHashCode.Include
    private long id;
    private DayOfWeek dayOfWeek;
    private String time;

    private long therapistId;
}
