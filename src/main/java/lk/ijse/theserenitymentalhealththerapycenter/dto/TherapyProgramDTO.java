package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
public class TherapyProgramDTO {
    private String id;
    private String name;
    private String duration;
    private BigDecimal fee;
    private Integer totalSessions;
    private BigDecimal sessionFee;
    private String description;
    private List<PatientTherapyProgramDTO> patientTherapyPrograms;
    private List<TherapistDTO> therapists;

    public TherapyProgramDTO(long id, String name, String duration, BigDecimal fee, Integer totalSessions, BigDecimal sessionFee, String description) {
        this.setId(id);
        this.name = name;
        this.duration = duration;
        this.fee = fee;
        this.totalSessions = totalSessions;
        this.sessionFee = sessionFee;
        this.description = description;
    }

    public void setId(long id) {
        this.id = String.format("PR%03d", id);
    }

    public long getId() {
        if (this.id != null && this.id.startsWith("PR")) {
            return Long.parseLong(this.id.substring(2));
        }
        return 0;
    }
    public String getStringId() {
        return this.id;
    }
}
