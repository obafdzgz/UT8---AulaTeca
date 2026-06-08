# Especificaciones del Modelo de Negocio - Sistema Aulateca

Este documento detalla la lógica de negocio, las reglas operativas y las especificaciones de datos que rigen el funcionamiento del Sistema de Gestión de Reservas **Aulateca**.

## 1. Entidades Principales del Dominio
El sistema se compone de seis entidades principales mapeadas mediante un Enfoque de Diseño Orientado a Objetos (JPA/Hibernate):

* **User (Usuario):** Representa a cualquier persona con credenciales de acceso al sistema (Administradores, Profesores, Alumnos). Contiene datos de autenticación y una asignación de rol rígida basada en tipos enumerados (`Role`).
* **ResourceType (Tipo de Recurso):** Clasificación maestra que segmenta los recursos del centro escolar en categorías lógicas (por ejemplo: "Aula de Informática", "Proyector Portátil", "Carrito de Tablets").
* **ResourceStatus (Estado del Recurso):** Tabla maestra que dictamina la disponibilidad operativa del recurso. Los estados permitidos y normalizados en el sistema son `"OPERATIVO"`, `"MANTENIMIENTO"` y `"FUERA_DE_SERVICIO"`.
* **Resource (Recurso):** El bien físico o aula concreta susceptible de ser reservado (ejemplo: "Aula 104", "Proyector B"). Cada recurso está obligatoriamente vinculado a un Tipo y a un Estado.
* **TimeSlot (Franja Horaria):** Define los intervalos de tiempo preestablecidos por el centro escolar para las reservas (ejemplo: "08:00 - 09:00", "09:00 - 10:00"). Se gestiona mediante tipos de tiempo nativos (`LocalTime`).
* **Reservation (Reserva):** El registro transaccional central que consolida la asignación de un recurso específico a un usuario en una fecha (`LocalDate`) y franja horaria (`TimeSlot`) determinadas.

## 2. Reglas de Funcionamiento y Restricciones de Disponibilidad
El negocio impone directrices estrictas para garantizar el orden y uso óptimo de las instalaciones y materiales didácticos. Estas reglas se ejecutan de manera secuencial en la capa de lógica del negocio (`ReservationService`) antes de persistir cualquier registro:

1. **Validación Temporal Crítica:** El sistema prohíbe de forma absoluta agendar reservas en fechas anteriores a la fecha actual del servidor (`LocalDate.now()`).
2. **Restricción de Estado Operativo:** No se puede reservar ningún recurso cuyo estado actual sea diferente de `"OPERATIVO"`. Si un recurso se encuentra marcado en estado `"MANTENIMIENTO"` debido a averías o revisiones técnicas, la capa de servicio bloquea la transacción lanzando un `IllegalStateException` y deniega la reserva de inmediato.

## 3. Proceso de Reserva e Integridad Antiduplicados
El proceso para consolidar una reserva sigue un flujo de control de concurrencia y validación en dos niveles de seguridad:

### A. Validación Lógica por Capa de Servicio
Cuando la capa de presentación solicita registrar una reserva, el método `registrarReserva()` invoca de manera obligatoria a la función específica del repositorio `existsReservation(resourceId, slotId, date)`. Esta función realiza una consulta HQL parametrizada que cuenta los registros coincidentes en la base de datos en tiempo real. Si el contador es mayor a cero, significa que el recurso ya está comprometido por otro docente, interrumpiendo el flujo con una excepción de estado ilegal.

### B. Blindaje Físico en Base de Datos (Unicidad Compuesta)
Como medida de seguridad definitiva frente a accesos simultáneos o problemas de concurrencia de red (donde dos usuarios pulsen el botón en el mismo milisegundo), la entidad `Reservation` implementa un índice de **unicidad compuesta** en su metadata de persistencia:

```java
@Table(name = "reservations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resource_id", "slot_id", "reservation_date"})
})