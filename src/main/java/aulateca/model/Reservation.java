package aulateca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "reservations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resource_id", "slot_id", "reservation_date"})
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String observations;

    public Reservation() {}

    public Reservation(User user, Resource resource, TimeSlot timeSlot, LocalDate date) {
        this.user = user;
        this.resource = resource;
        this.timeSlot = timeSlot;
        this.date = date;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }
}