package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProgramPaymentTM {
    private TherapyProgramDTO program;
    private String programName;
    private int totalSessions;
    private int sessionsToPay;
    private BigDecimal subtotal;
}
