package smsbot;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by shohei on 3/13/16.
 */
public class Student {
    private final SimpleStringProperty firstName;
    private final SimpleStringProperty lastName;
    private final SimpleStringProperty telephone;

//    private final int numSubjects;
//    private final List<SimpleIntegerProperty> subjects;

    public Student(String fName, String lName, String telephone) {
        this.firstName = new SimpleStringProperty(fName);
        this.lastName = new SimpleStringProperty(lName);
        this.telephone = new SimpleStringProperty(telephone);
//        this.numSubjects = numSubjects;

//        subjects =  new ArrayList<>();
//
//        for(int i=0;i<numSubjects;i++){
//            subjects.add(i,new SimpleIntegerProperty(...));
//        }

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
