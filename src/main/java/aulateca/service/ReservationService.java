package aulateca.service;

import aulateca.model.Reservation;
import aulateca.dao.ReservationDAO;

import java.time.LocalDate;

public class ReservationService {

    // Necesitamos el DAO para interactuar con la base de datos
    private final ReservationDAO reservationDAO = new ReservationDAO();

    //Registrar una reserva validando restricciones

    public void registrarReserva(Reservation reserva) {

        //Validación de Fecha
        if (reserva.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se pueden realizar reservas en fechas pasadas.");
        }

        // Validación de Estado del Recurso (Debe estar operativo)
        String estado = reserva.getResource().getStatus().getName();
        if (!"OPERATIVO".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException("El recurso '" + reserva.getResource().getName() +
                    "' no se puede reservar porque su estado actual es: " + estado);
        }

        //Validación de Disponibilidad (Evitar duplicados)
        boolean yaOcupado = reservationDAO.existsReservation(
                reserva.getResource().getId(),
                reserva.getTimeSlot().getId(),
                reserva.getDate()
        );

        if (yaOcupado) {
            throw new IllegalArgumentException("Error: El recurso ya está reservado en esa fecha y franja horaria.");
        }

        // Si pasa todos los filtros, guardar
        try {
            reservationDAO.save(reserva);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error interno al procesar la reserva: " + e.getMessage());
        }
    }
}