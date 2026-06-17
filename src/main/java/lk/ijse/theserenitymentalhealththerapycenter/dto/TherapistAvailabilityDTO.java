package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TherapistAvailabilityDTO {

    private long id;
    private LocalDate date;
    private String time;

    private long therapistId;
}
