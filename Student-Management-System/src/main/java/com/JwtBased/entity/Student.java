package com.JwtBased.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ✅ Join with users table via user_id
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	// ✅ Only student-specific important fields
	// fullName, phone, address, DOB, profilePicture → already in users table
	@Column(unique = true)
	private String rollNumber;

	private String className;
	private String section;
	private String parentName;
	private String parentPhone;
	private LocalDate admissionDate;
}