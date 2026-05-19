package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TherapistTM {
    private String id;
    private String name;
    private String specialty;
    private String phone;
    private String email;
    private TherapistStatus status;
}
