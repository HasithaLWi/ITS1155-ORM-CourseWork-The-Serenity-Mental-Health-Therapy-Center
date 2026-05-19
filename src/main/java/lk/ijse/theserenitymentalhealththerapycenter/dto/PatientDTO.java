package lk.ijse.theserenitymentalhealththerapycenter.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@lombok.NoArgsConstructor
@lombok.Data
public class PatientDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate registeredDate;
    private ArrayList<TherapyProgramDTO> programs;
    private Map<Long, Integer> upfrontSessionsPerProgram = new HashMap<>(); // programId -> sessions paid upfront
    private String interviewNote;

    public PatientDTO(long id, String name, String email, String phone, String address, LocalDate registeredDate, ArrayList<TherapyProgramDTO> programs) {
        this.id = String.format("P%03d", id); // P001, P002, ...
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registeredDate = registeredDate;
        this.programs = programs;
    }

    public void setId(long id) {
        this.id = String.format("P%03d", id); // P001, P002, ...
    }
    public long getId() {
        String pId = this.id.substring(1); // Remove 'P' prefix
        return Long.parseLong(pId);
    }
    public String getStringId() {
        return this.id;
    }
}
