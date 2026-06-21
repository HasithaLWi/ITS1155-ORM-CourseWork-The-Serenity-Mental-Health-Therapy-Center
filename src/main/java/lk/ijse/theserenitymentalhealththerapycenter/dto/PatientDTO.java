package lk.ijse.theserenitymentalhealththerapycenter.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@lombok.NoArgsConstructor
@lombok.Data
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PatientDTO {
    @lombok.EqualsAndHashCode.Include
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String genderIdentity;
    private String email;
    private String phone;
    private String address;

    // Emergency Contact
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    // Insurance Details (nullable)
    private String insuranceProvider;
    private String insurancePolicyId;
    private String insuranceGroupNumber;

    // Status
    private String status = "ACTIVE";

    private LocalDate registeredDate;
    private ArrayList<TherapyProgramDTO> programs;
    private Map<Long, Integer> upfrontSessionsPerProgram = new HashMap<>();
    private String interviewNote;

    private PaymentDTO upfrontPayment;

    public PatientDTO(long id, String firstName, String lastName, String email, String phone, String address, LocalDate registeredDate, ArrayList<TherapyProgramDTO> programs) {
        this.id = String.format("P%03d", id); // P001, P002, ...
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.registeredDate = registeredDate;
        this.programs = programs;
    }

    /**
     * Returns the full name by combining first and last name.
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
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
