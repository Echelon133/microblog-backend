package ml.echelon133.microblog.user.model;

import org.hibernate.validator.constraints.Length;

public class UserDetailsDto {

    @Length(min = 1, max = 70, message = "Username length is invalid")
    private String displayedUsername;

    @Length(min = 1, max = 200, message = "Description length is invalid")
    private String description;

    private String aviURL;

    public String getDisplayedUsername() {
        return displayedUsername;
    }

    public void setDisplayedUsername(String displayedUsername) {
        this.displayedUsername = displayedUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAviURL() {
        return aviURL;
    }

    public void setAviURL(String aviURL) {
        this.aviURL = aviURL;
    }
}
