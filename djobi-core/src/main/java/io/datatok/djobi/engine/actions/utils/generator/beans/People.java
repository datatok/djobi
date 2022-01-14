package io.datatok.djobi.engine.actions.utils.generator.beans;

import java.io.Serializable;

public class People implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;

    private int age;

    private String sexe;

    public People() {
    }

    public String getFirstName() {
        return firstName;
    }

    public People setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public People setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public int getAge() {
        return age;
    }

    public People setAge(int age) {
        this.age = age;
        return this;
    }

    public String getSexe() {
        return sexe;
    }

    public People setSexe(String sexe) {
        this.sexe = sexe;
        return this;
    }
}
