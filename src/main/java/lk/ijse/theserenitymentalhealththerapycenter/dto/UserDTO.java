package lk.ijse.theserenitymentalhealththerapycenter.dto;

import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDTO {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private UserRole role;

    public UserDTO(long id, String username, String password, String fullName, String email, UserRole role) {
        this.setId(id);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public void setId(long id) {
        this.id = String.format("U%03d", id);
    }

    public long getId() {
        if (this.id != null && this.id.startsWith("U")) {
            return Long.parseLong(this.id.substring(1));
        }
        return 0;
    }
}
