package ml.echelon133.microblog.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import ml.echelon133.microblog.user.validators.PasswordsMatch;
import ml.echelon133.microblog.user.validators.ValidPassword;
import ml.echelon133.microblog.user.validators.ValidUsername;
import org.hibernate.validator.constraints.Length;

@PasswordsMatch
public class NewUserDto {

    @ValidUsername
    private String username;

    @NotEmpty(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @Length(min = 8, max = 64, message = "Expected password length between 8 and 64 characters")
    @ValidPassword
    private String password;
    private String password2;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }
}
