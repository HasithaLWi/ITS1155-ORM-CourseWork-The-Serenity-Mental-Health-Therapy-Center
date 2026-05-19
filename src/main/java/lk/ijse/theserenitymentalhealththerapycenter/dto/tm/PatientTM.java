package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PatientTM {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String registeredDate; // formatted string for display
}
