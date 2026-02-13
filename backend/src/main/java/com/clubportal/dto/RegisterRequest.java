package com.clubportal.dto;

public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    // Optional: allow the frontend to specify account type. We only accept STUDENT or CLUB_LEADER.
    private String role;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
