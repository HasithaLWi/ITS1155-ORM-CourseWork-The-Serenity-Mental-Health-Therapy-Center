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
    private String patientPhone;

    public TherapySessionTM(String id, Integer sequenceNumber, LocalDate sessionDate, LocalTime sessionTime, String status, String paymentStatus, String patientName, String therapistName, String programName) {
        this.id = id;
        this.sequenceNumber = sequenceNumber;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.patientName = patientName;
        this.therapistName = therapistName;
        this.programName = programName;
        this.patientPhone = null;
    }
}
