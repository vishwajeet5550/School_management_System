// LeaveRequestDTO.java — INPUT
package com.JwtBased.dto;

import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LeaveRequestDTO {
    private String leaveType;   // SICK, PERSONAL, FAMILY, OTHER
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
}