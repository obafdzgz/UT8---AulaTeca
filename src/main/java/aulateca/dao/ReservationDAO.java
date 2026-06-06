package aulateca.dao;

import aulateca.model.Reservation;
import aulateca.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;

// Hereda todos los métodos de GenericDAO para CRUD básico
// //añade un metodo específico para verificar reservas
public class ReservationDAO extends GenericDAO<Reservation> {

    // Método crucial: verifica si ya existe una reserva idéntica
    public boolean existsReservation(Integer resourceId, Integer slotId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(r) FROM Reservation r " +
                    "WHERE r.resource.id = :resId " +
                    "AND r.timeSlot.id = :slotId " +
                    "AND r.date = :resDate";

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("resId", resourceId);
            query.setParameter("slotId", slotId);
            query.setParameter("resDate", date);

            Long count = query.uniqueResult();
            return count != null && count > 0; // Devuelve true si ya está ocupado
        } catch (Exception e) {
            throw new RuntimeException("Error al comprobar disponibilidad: " + e.getMessage(), e);
        }
    }
}