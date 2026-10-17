    package com.careerosx.backend.dto;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    public class UpdateProfileRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        private String email;
    
        public void setEmail(String email) {
            this.email = email;
        }
        public String getEmail() {
            return email;
        }
    }