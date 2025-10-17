package SampleJWT.auth.dto;

public class UserDTO {
    private String collegeId;
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private String role;

    // Constructors
    public UserDTO() {}

    public UserDTO(String collegeId, String firstname, String lastname, String email, String username, String role) {
        this.collegeId = collegeId;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.username = username;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
