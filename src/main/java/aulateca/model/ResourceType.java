package aulateca.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "resource_types")
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // Constructor vacío
    public ResourceType() {
    }

    // Constructor con parámetros
    public ResourceType(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ResourceType{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}