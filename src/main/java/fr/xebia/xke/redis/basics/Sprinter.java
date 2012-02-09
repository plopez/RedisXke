package fr.xebia.xke.redis.basics;


import java.io.Serializable;

public class Sprinter implements Serializable {
    public String name;
    private String firstName;
    private String country;

    public Sprinter(String name, String firstName, String country) {
        this.name = name;
        this.firstName = firstName;
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sprinter sprinter = (Sprinter) o;

        if (country != null ? !country.equals(sprinter.country) : sprinter.country != null) return false;
        if (firstName != null ? !firstName.equals(sprinter.firstName) : sprinter.firstName != null) return false;
        if (name != null ? !name.equals(sprinter.name) : sprinter.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}
