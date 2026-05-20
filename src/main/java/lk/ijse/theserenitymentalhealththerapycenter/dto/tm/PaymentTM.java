package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentTM {
    private String id;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String method;
    private String status;
    private String paymentType;
    private String patientName;
    private String sessionId; // E.g., S001
    private String description;
    private Long patientId;
}
