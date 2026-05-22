package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramSummaryDTO {
    private String name;
    private String duration;
    private BigDecimal fee;
    private Long enrolledPatients;
}

