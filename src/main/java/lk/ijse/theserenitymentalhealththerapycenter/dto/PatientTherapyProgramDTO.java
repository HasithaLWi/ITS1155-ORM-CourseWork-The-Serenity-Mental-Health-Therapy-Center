package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class PatientTherapyProgramDTO {
    private String id;
    private Long patientId;
    private Long programId;
    private String programName;
    private int sessionsPaid;
    private int sessionsUsed;
    private int totalSessions; // from program
    private TherapyProgramDTO program;
    private PatientDTO patient;

    public PatientTherapyProgramDTO(Long id, Long patientId, Long programId, int sessionsPaid, int sessionsUsed) {
        this.setId(id);
        this.patientId = patientId;
        this.programId = programId;
        this.sessionsPaid = sessionsPaid;
        this.sessionsUsed = sessionsUsed;
    }

    public void setId(Long id) {
        this.id = String.format("PTP%03d", id);
    }

    public Long getId() {
        if (this.id != null && this.id.startsWith("PTP")) {
            return Long.parseLong(this.id.substring(3));
        }
        return null;
    }

    public int getRemainingCredit() {
        return sessionsPaid - sessionsUsed;
    }
}
