package lk.ijse.theserenitymentalhealththerapycenter.dto;
import lk.ijse.theserenitymentalhealththerapycenter.enumaration.TherapistStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TherapistDTO {
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private String specialty;
    private String phone;
    private String email;
    private TherapistStatus status;
    private List<Long> programIds = new ArrayList<>();

    public TherapistDTO(long id, String name, String specialty, String phone, String email, TherapistStatus status) {
        this.setId(id);
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.email = email;
        this.status = status;
    }

    public void setId(long id) {
        this.id = String.format("T%03d", id);
    }

    public long getId() {
        if (this.id != null && this.id.startsWith("T")) {
            return Long.parseLong(this.id.substring(1));
        }
        return 0;
    }

    public String getStringId() {
        return this.id;
    }
}
