package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDeleteSummaryDTO {
    private String patientName;
    private int programCount;
    private int sessionCount;
    private int paymentCount;
    private double totalPaidAmount;
}
