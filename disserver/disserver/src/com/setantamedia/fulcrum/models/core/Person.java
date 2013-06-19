package com.setantamedia.fulcrum.models.core;

import com.setantamedia.fulcrum.common.Site;
import org.json.JSONObject;

public class Person extends Entity {

    private String username = "";
    private String ssoUsername = "";
    private String password = "";
    private String damAccess = "";
    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private Boolean loginActive = false;
    private String primaryRole = "";
    private String[] roles = new String[0];
    private Boolean adminUser = false;
    private Boolean guestUser = false;
    private String ticket = null;
    private Boolean resetPassword = false;
    private Site[] sites = new Site[0];
    public final static String FIELD_FIRST_NAME = "firstName";
    public final static String FIELD_LAST_NAME = "lastName";
    public final static String FIELD_FULL_NAME = "fullName";
    public final static String FIELD_USERNAME = "username";
    public final static String FIELD_SSOUSERNAME = "ssousername";
    public final static String FIELD_EMAIL = "email";
    public final static String FIELD_RESET_PASSWORD = "resetPassword";

    public Person() {
    }

    public Boolean getResetPassword() {
        return resetPassword;
    }

    public void setResetPassword(Boolean resetPassword) {
        this.resetPassword = resetPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public Boolean getLoginActive() {
        return loginActive;
    }

    public void setLoginActive(Boolean loginActive) {
        this.loginActive = loginActive;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrimaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public String getSsoUsername() {
        return ssoUsername;
    }

    public void setSsoUsername(String ssoUsername) {
        this.ssoUsername = ssoUsername;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Boolean isAdminUser() {
        return getAdminUser();
    }

    public Boolean getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(Boolean adminUser) {
        this.adminUser = adminUser;
    }

    public Boolean getGuestUser() {
        return guestUser;
    }

    public void setGuestUser(Boolean guestUser) {
        this.guestUser = guestUser;
    }

    public Site[] getSites() {
        return sites;
    }

    public void setSites(Site[] sites) {
        this.sites = sites;
    }

    public String getDamAccess() {
        return damAccess;
    }

    public void setDamAccess(String damAccess) {
        this.damAccess = damAccess;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public JSONObject toJson() {
        JSONObject result = super.toJson();
        try {
            result.put(FIELD_FIRST_NAME, firstName);
            result.put(FIELD_LAST_NAME, lastName);
            result.put(FIELD_USERNAME, username);
            result.put(FIELD_SSOUSERNAME, ssoUsername);
            result.put(FIELD_EMAIL, email);
            result.put(FIELD_RESET_PASSWORD, resetPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
