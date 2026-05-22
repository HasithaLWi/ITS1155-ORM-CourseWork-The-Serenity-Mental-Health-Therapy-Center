package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSummaryTM {
    private String name;
    private String duration;
    private BigDecimal fee;
    private Long enrolledPatients;
}
