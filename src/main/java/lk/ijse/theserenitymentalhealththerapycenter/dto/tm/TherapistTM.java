package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class TherapistTM {
    private String id;
    private String name;
    private String specialty;
    private String phone;
    private String email;
    private TherapistStatus status;
    private List<Long> programIds = new ArrayList<>();

    public TherapistTM(String id, String name, String specialty, String phone, String email, TherapistStatus status) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    public TherapistTM(String id, String name, String specialty, String phone, String email, TherapistStatus status, List<Long> programIds) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.programIds = programIds;
    }
}
