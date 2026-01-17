package com.clubportal.dto;

public class GoogleLoginRequest {
    private String credential; // 前端传来的 Google ID Token

    public String getCredential() { return credential; }
    public void setCredential(String credential) { this.credential = credential; }
}
