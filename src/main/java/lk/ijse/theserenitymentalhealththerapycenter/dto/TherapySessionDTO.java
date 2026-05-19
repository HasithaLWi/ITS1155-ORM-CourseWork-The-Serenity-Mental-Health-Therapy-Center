package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Data
public class TherapySessionDTO {
    private String id;
    private Integer sequenceNumber;
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private SessionStatus status;
    private SessionPaymentStatus paymentStatus;
    private String notes;
    
    // Relationships represented by IDs
    private Long patientId;
    private Long therapistId;
    private Long programId;
    private Long paymentId;
    private Long upfrontPaymentId;

    // Display helpers (for TM/table use)
    private String patientName;
    private String therapistName;
    private String programName;

    public TherapySessionDTO(long id, Integer sequenceNumber, LocalDate sessionDate, LocalTime sessionTime, SessionStatus status, SessionPaymentStatus paymentStatus, String notes, Long patientId, Long therapistId, Long programId) {
        this.setId(id);
        this.sequenceNumber = sequenceNumber;
        this.sessionDate = sessionDate;
        this.sessionTime = sessionTime;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.notes = notes;
        this.patientId = patientId;
        this.therapistId = therapistId;
        this.programId = programId;
    }

    public void setId(long id) {
        this.id = String.format("S%03d", id);
    }

    public long getId() {
        if (this.id != null && this.id.startsWith("S")) {
            return Long.parseLong(this.id.substring(1));
        }
        return 0;
    }

    public String getStringId() {
        return this.id;
    }
}
