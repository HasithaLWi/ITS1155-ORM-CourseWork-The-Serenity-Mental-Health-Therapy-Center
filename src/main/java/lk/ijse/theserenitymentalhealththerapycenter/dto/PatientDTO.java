package lk.ijse.theserenitymentalhealththerapycenter.dto;


import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;

import java.time.LocalDate;
import java.util.ArrayList;


@lombok.NoArgsConstructor
@lombok.Data
public class PatientDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private LocalDate registeredDate;
    private ArrayList<TherapyProgram> programs; // programId -> programName
    private ArrayList<TherapySession> sessions; // sessionId -> sessionDate

    public PatientDTO(long id, String name, String email, String phone, String address, LocalDate registeredDate, ArrayList<TherapyProgram> programs, ArrayList<TherapySession> sessions) {
        this.id = String.format("P%03d", id); // P001, P002, ...
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registeredDate = registeredDate;
        this.programs = programs;
        this.sessions = sessions;
    }

    public void setId(long id) {
        this.id = String.format("P%03d", id); // P001, P002, ...
    }
    public long getId() {
        String pId = this.id.substring(1); // Remove 'P' prefix
        return Long.parseLong(pId);
    }
}
