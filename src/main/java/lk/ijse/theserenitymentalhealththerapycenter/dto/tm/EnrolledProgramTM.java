package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class EnrolledProgramTM {
    public final String programName;
    public final int totalSessions;
    public final int sessionsPaid;
    public final int sessionsUsed;
    public final int creditRemaining;
    public final int completedSessions;
    public final String status;

}
