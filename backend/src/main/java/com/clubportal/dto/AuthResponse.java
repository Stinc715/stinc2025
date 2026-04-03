package com.clubportal.dto;

public class AuthResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String role;
    private String authProvider;

    public AuthResponse(Integer id, String fullName, String email, String role, String authProvider) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.authProvider = authProvider;
    }

    public Integer getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAuthProvider() { return authProvider; }
}
