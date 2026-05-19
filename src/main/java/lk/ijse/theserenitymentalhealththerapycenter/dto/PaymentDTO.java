package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class PaymentDTO {
    private String id;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMethod method;
    private PaymentStatus status;
    private PaymentType paymentType;
    private BigDecimal discount;
    private String description;
    
    // Relationships
    private Long sessionId;
    private Long patientId;
    
    // Display helpers (for TM/table use)
    private String patientName;
    private String sessionInfo;

    public PaymentDTO(long id, BigDecimal amount, LocalDateTime paymentDate, PaymentMethod method, PaymentStatus status, PaymentType paymentType, BigDecimal discount, String description, Long sessionId, Long patientId) {
        this.setId(id);
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.method = method;
        this.status = status;
        this.paymentType = paymentType;
        this.discount = discount;
        this.description = description;
        this.sessionId = sessionId;
        this.patientId = patientId;
    }

    public void setId(long id) {
        this.id = String.format("PAY%03d", id);
    }

    public long getId() {
        if (this.id != null && this.id.startsWith("PAY")) {
            return Long.parseLong(this.id.substring(3));
        }
        return 0;
    }
}
