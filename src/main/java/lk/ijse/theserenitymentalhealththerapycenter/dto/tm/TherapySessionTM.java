package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TherapySessionTM {
    private String id;
    private Integer sequenceNumber;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private String status;
    private String paymentStatus;
    private String patientName;
    private String therapistName;
    private String programName;
}
