package com.clubportal.dto;

public class GoogleLoginRequest {
    private String credential; // Google ID token received from the frontend.

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
