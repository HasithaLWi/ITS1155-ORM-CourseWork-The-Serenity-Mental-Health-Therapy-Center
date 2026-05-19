package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TherapyProgramTM {
    private String id;
    private String name;
    private String duration;
    private BigDecimal fee;
    private Integer totalSessions;
    private BigDecimal sessionFee;
    private String description;

    public Long getId() {
        if (this.id != null && this.id.startsWith("PR")) {
            return Long.parseLong(this.id.substring(2));
        }
        return null;
    }

    public String getStringId() {
        return this.id;
    }

}
