
/**
 * Represents a user in the system.
 */

import java.sql.Date;

public class User {

    private final String username;
    private final String name;
    private final String surname;
    private final Date birthdate;
    private final String gender;
    private final String email;
    private final String location;
    private final String password;
    private final boolean isAdmin;

    /**
     * Constructs a new user with specified parameters
     *
     * @param username  the username of the user
     * @param name      the name of the user
     * @param surname   the surname of the user
     * @param birthdate the birthdate of the user in format YYYY-MM-DD
     * @param gender    the gender of the user, M or F
     * @param email     the email address of the user
     * @param location  the location of the user
     * @param password  the password of the user
     * @param isAdmin   true if the user is an admin, false otherwise
     */
    public User(String username, String name, String surname, Date birthdate, String gender, String email,
            String location, String password, boolean isAdmin) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.email = email;
        this.location = location;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // GETTERS

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @return the birthdate
     */
    public Date getBirthdate() {
        return birthdate;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isAdmin;
    }
}
