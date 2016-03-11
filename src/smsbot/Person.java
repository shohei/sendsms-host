package smsbot;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by shohei on 3/11/16.
 */
public class Person {
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty telephone;

    public Person(String fName, String lName, String telephone) {
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
        this.telephone = new SimpleStringProperty(telephone);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getTelephone() {
        return telephone.get();
    }

    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }
}
