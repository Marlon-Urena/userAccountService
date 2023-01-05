package com.chatapp.model;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Entity
@Table(name = "user_account")
public class UserAccount {
  @Id
  private String uid;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(unique = true, nullable = false)
  private String username;

  private String firstName;
  private String lastName;
  private String address;
  private String city;
  private String state;
  private String country;
  private String zipCode;
  private String status = "offline";
  @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private OffsetDateTime lastActivity = OffsetDateTime.now();
  private String phoneNumber;

  private String photoUrl;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    UserAccount user = (UserAccount) o;
    return uid.equals(user.uid)
        && email.equals(user.email)
        && username.equals(user.username)
        && firstName.equals(user.firstName)
        && lastName.equals(user.lastName)
        && address.equals(user.address)
        && city.equals(user.city)
        && state.equals(user.state)
        && country.equals(user.country)
        && zipCode.equals(user.zipCode)
        && status.equals(user.status)
        && lastActivity.isEqual(user.lastActivity)
        && phoneNumber.equals(user.phoneNumber)
        && photoUrl.equals(user.photoUrl);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
