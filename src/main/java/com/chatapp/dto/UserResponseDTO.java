package com.chatapp.dto;

import javax.persistence.Column;
import java.time.OffsetDateTime;
import java.util.Objects;

public class UserResponseDTO {
    private String uid;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String status = "offline";
    private OffsetDateTime lastActivity = OffsetDateTime.now();
    private String phoneNumber;
    private String photoUrl;

    public UserResponseDTO() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(OffsetDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDTO that = (UserResponseDTO) o;
        return Objects.equals(uid, that.uid) && Objects.equals(email, that.email) && Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(address, that.address) && Objects.equals(city, that.city) && Objects.equals(state, that.state) && Objects.equals(country, that.country) && Objects.equals(zipCode, that.zipCode) && Objects.equals(status, that.status) && Objects.equals(lastActivity, that.lastActivity) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(photoUrl, that.photoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, email, username, firstName, lastName, address, city, state, country, zipCode, status, lastActivity, phoneNumber, photoUrl);
    }

    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", status='" + status + '\'' +
                ", lastActivity=" + lastActivity +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                '}';
    }
}
