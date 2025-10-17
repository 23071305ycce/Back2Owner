package SampleJWT.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterDTO {
    @JsonProperty("collegeId")
    private String collegeId;

    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String password;

    // Constructors
    public RegisterDTO() {}

    public RegisterDTO(String collegeId, String firstname, String lastname, String email, String username, String password) {
        this.collegeId = collegeId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(String collegeId) {
        this.collegeId = collegeId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
