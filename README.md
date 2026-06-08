# Sistema Aulateca - Gestión de Reservas

¡Bienvenido al repositorio del proyecto **Aulateca**! Esta aplicación de escritorio ha sido desarrollada como una solución robusta y desacoplada para gestionar las reservas de espacios, aulas y recursos compartidos de un centro educativo.

---

## Estructura de la Documentación Oficial

En cumplimiento con los requisitos de evaluación descritos en el pliego del proyecto, la documentación detallada se encuentra dividida en los siguientes ficheros independientes en la raíz del repositorio:

1. **[Especificaciones del Modelo de Negocio](ESPECIFICACIONES_MODELO_NEGOCIO.md):** Explicación de las entidades del dominio, lógica de negocio de los tramos horarios, control de recursos en mantenimiento (Punto 10) y el sistema de seguridad contra solapamientos o reservas duplicadas (Punto 11).
2. **[Especificaciones Técnicas](ESPECIFICACIONES_TECNICAS.md):** Arquitectura del software estructurada en capas (MVC + Capa de Servicio), justificación del uso de Java 21, rendimiento de las consultas ORM con Hibernate (`@ManyToOne` + `FetchType.EAGER`), patrón repositorio genérico y decisiones técnicas sobre el entorno de desarrollo.
3. **[Registro de Prompts de IA](REGISTRO_PROMPTS_IA.md):** Documento de transparencia que recopila las consultas críticas realizadas a herramientas de Inteligencia Artificial para la resolución de errores del compilador y la optimización de código.

---

## Estructura del Proyecto

```
AulaTecaProject/
├── db/
│   └── Dump20260607/              # Scripts SQL de carga de datos iniciales
│       ├── aulateca_db_users.sql
│       ├── aulateca_db_resources.sql
│       ├── aulateca_db_resource_types.sql
│       ├── aulateca_db_resource_status.sql
│       ├── aulateca_db_time_slots.sql
│       └── aulateca_db_reservations.sql
├── src/
│   └── main/
│       ├── java/aulateca/
│       │   ├── dao/               # Capa de acceso a datos (GenericDAO, ReservationDAO, UserDAO)
│       │   ├── model/             # Entidades JPA (User, Resource, Reservation, TimeSlot...)
│       │   ├── service/           # Lógica de negocio (ReservationService, UserService)
│       │   ├── util/              # Utilidades transversales (HibernateUtil)
│       │   ├── view/              # Interfaz gráfica Swing (12 ventanas/diálogos)
│       │   └── main/Main.java     # Punto de entrada de la aplicación
│       └── resources/
│           └── hibernate.cfg.xml  # Configuración de conexión a MySQL
├── pom.xml                        # Dependencias Maven
├── ESPECIFICACIONES_MODELO_NEGOCIO.md
├── ESPECIFICACIONES_TECNICAS.md
└── REGISTRO_PROMPTS_IA.md
```

---

## Requisitos del Sistema e Infraestructura

Para ejecutar y evaluar este proyecto de forma local, asegúrese de contar con el siguiente entorno configurado:

| Componente | Versión requerida |
|---|---|
| **JDK** | Java 21 LTS (Standard Edition) |
| **Maven** | 3.8 o superior |
| **Base de datos** | MySQL Server 8.0+ o MariaDB equivalente |
| **IDE recomendado** | IntelliJ IDEA |

### Dependencias Maven (`pom.xml`)

| Librería | Versión | Propósito |
|---|---|---|
| `hibernate-core` | 6.4.4.Final | ORM / mapeo objeto-relacional |
| `mysql-connector-j` | 8.3.0 | Driver JDBC para MySQL |
| `jakarta.persistence-api` | 3.1.0 | API estándar JPA |
| `LGoodDatePicker` | 11.2.1 | Selector de fechas en la UI |
| `lombok` | 1.18.30 | Anotaciones de código (declarado, no activo) |

---

## Instrucciones de Configuración y Despliegue

Siga estos pasos de manera secuencial para arrancar la aplicación en su máquina:

### 1. Preparación de la Base de Datos

1. Inicie su servidor local de bases de datos (XAMPP, Docker o servicio nativo de MySQL).
2. Cree una base de datos limpia ejecutando la siguiente sentencia SQL:
   ```sql
   CREATE DATABASE aulateca_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Importe los scripts de datos iniciales en orden desde la carpeta `db/Dump20260607/`:
   ```bash
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_resource_types.sql
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_resource_status.sql
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_resources.sql
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_users.sql
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_time_slots.sql
   mysql -u root -p aulateca_db < db/Dump20260607/aulateca_db_reservations.sql
   ```

### 2. Configuración de la Conexión

Edite el fichero `src/main/resources/hibernate.cfg.xml` y ajuste las credenciales de acceso a su instancia local de MySQL:

```xml
<property name="connection.url">jdbc:mysql://localhost:3306/aulateca_db</property>
<property name="connection.username">root</property>
<property name="connection.password">SU_CONTRASEÑA_AQUI</property>
```

> **Importante:** La propiedad `hbm2ddl.auto` está configurada en `update`, lo que permite que Hibernate cree o actualice automáticamente el esquema de tablas al arrancar la aplicación si aún no existe.

### 3. Compilar y Ejecutar con Maven

Desde la raíz del proyecto, ejecute los siguientes comandos en su terminal:

```bash
# Compilar el proyecto y descargar dependencias
mvn clean compile

# Ejecutar la aplicación
mvn exec:java -Dexec.mainClass="aulateca.main.Main"
```

Alternativamente, desde IntelliJ IDEA basta con abrir el proyecto, dejar que Maven resuelva las dependencias automáticamente y ejecutar la clase `Main.java` directamente.

---

## Guía de la Interfaz Gráfica

La aplicación consta de **12 ventanas y diálogos** implementados en Java Swing puro con `GridBagLayout`, sin uso de diseñadores visuales de arrastrar y soltar.

### Pantalla de Login

Al arrancar la aplicación, se muestra la ventana de inicio de sesión. Introduce un usuario y contraseña de los registrados en la base de datos.

> **Credenciales de prueba precargadas en el dump:**

| Usuario | Contraseña | Rol | Nombre completo |
|---|---|---|---|
| `oba` | `1234` | ADMIN | Obal dai Fernández Gazulla |
| `celeste` | `1234` | ADMIN | Celeste Arbelo Garcia |
| `saul` | `1234` | PROFESOR | Saul Profe Programacion |
| `marco` | `1234` | ALUMNO | Marco Suarez |

### Panel Principal (Dashboard)

Tras autenticarse, el sistema redirige al **panel de control principal**, que adapta su contenido según el rol del usuario:

| Tarjeta | Disponible para | Descripción |
|---|---|---|
| 🔵 **Nueva Reserva** | Todos | Lanza el asistente de reservas en 3 pasos |
| 🟢 **Ver Horarios** | Todos | Consulta la disponibilidad por recurso y fecha |
| 🟠 **Mis Reservas** | Todos | Historial personal de reservas realizadas |
| ⚫ **Gestión General** | ADMIN / PROFESOR | Panel de administración del centro |

La barra de menú superior también se adapta al rol: los usuarios con rol `ALUMNO` no ven el menú de **Gestión**.

### Asistente de Nueva Reserva (Wizard 3 pasos)

El módulo de reservas guía al usuario mediante un flujo de tres pasos secuenciales implementado con `CardLayout`:

1. **Paso 1 — Categoría:** El usuario selecciona el tipo de recurso que necesita (Aula, Proyector, Carrito de tablets, etc.).
2. **Paso 2 — Recurso concreto:** Se filtran y muestran únicamente los recursos operativos de la categoría elegida.
3. **Paso 3 — Fecha y franja horaria:** Se selecciona la fecha (con selector de calendario que bloquea fines de semana y fechas pasadas) y la franja horaria disponible. Las franjas ya ocupadas se deshabilitan automáticamente. Opcionalmente se pueden añadir observaciones.

### Consultar Disponibilidad

Permite filtrar por **recurso concreto** y **fecha** para obtener una tabla visual que muestra qué franjas horarias están libres (`LIBRE`) u ocupadas (`OCUPADO`) ese día.

### Historial de Reservas

Muestra todas las reservas del usuario en sesión ordenadas cronológicamente, con la opción de cancelar reservas futuras directamente desde la tabla.

### Módulos de Gestión (ADMIN / PROFESOR)

Accesibles desde el menú superior o la tarjeta de "Gestión General":

| Módulo | Descripción |
|---|---|
| **Usuarios** | CRUD completo de cuentas de usuario y asignación de roles |
| **Tipos de Recursos** | Gestión de las categorías maestras de recursos |
| **Recursos Concretos** | Alta, edición y baja de los recursos físicos del centro |
| **Estados de Recursos** | Gestión de los estados operativos (OPERATIVO, MANTENIMIENTO, FUERA_DE_SERVICIO) |
| **Franjas Horarias** | Definición de los intervalos horarios del centro |
| **Gestionar Reservas (Admin)** | Vista global de todas las reservas del sistema con capacidad de filtrado y cancelación |

---

## Arquitectura del Software

La aplicación implementa un patrón **MVC estricto con Capa de Servicio intermedia**, organizado en los siguientes paquetes:

```
aulateca.model      →  Entidades JPA (POJOs con anotaciones de persistencia)
aulateca.dao        →  Acceso a datos: GenericDAO<T> + DAOs especializados
aulateca.service    →  Lógica de negocio y validaciones de dominio
aulateca.view       →  Interfaz de usuario (Java Swing)
aulateca.util       →  HibernateUtil: gestión del SessionFactory singleton
```

### Diagrama de flujo de una reserva

```
LoginView → MainView → ReservationWizardView
                               ↓
                     ReservationService.registrarReserva()
                         ↓           ↓           ↓
                    ¿Fecha     ¿Estado      ¿Ya existe?
                    pasada?    OPERATIVO?   (antiduplicado)
                         ↓
                    ReservationDAO.save()
                         ↓
                    MySQL / aulateca_db
```

### Reglas de negocio aplicadas en `ReservationService`

1. **Validación temporal:** Se rechaza cualquier reserva con fecha anterior a `LocalDate.now()`.
2. **Validación de estado:** Solo se pueden reservar recursos en estado `OPERATIVO`. Los recursos en `MANTENIMIENTO` o `FUERA_DE_SERVICIO` son bloqueados.
3. **Control antiduplicados (doble capa):**
    - **Capa de servicio:** consulta HQL parametrizada que comprueba si ya existe una reserva para la combinación `(resource_id, slot_id, fecha)`.
    - **Capa de base de datos:** restricción de unicidad compuesta `UNIQUE(resource_id, slot_id, reservation_date)` como barrera definitiva ante accesos concurrentes.

---

## Modelo de Datos

```
users ──────────────────────────────────────────────────────────┐
  id, username, password, full_name, role(ALUMNO|PROFESOR|ADMIN) │
                                                                  │
resource_types          resource_status                           │
  id, name               id, name                                 │
      │                      │                                    │
      └──────┬───────────────┘                                    │
             ▼                                                    │
          resources ◄─────────────────── time_slots               │
   id, name, description   id, name, start_time, end_time         │
   type_id (FK)                          │                        │
   status_id (FK)                        │                        │
             │                           │                        │
             └────────────► reservations ◄────────────────────────┘
                      id, user_id, resource_id,
                      slot_id, reservation_date,
                      observations
                      UNIQUE(resource_id, slot_id, reservation_date)
```

---

## Notas Técnicas Relevantes

- **Lombok desactivado:** Aunque la dependencia permanece declarada en el `pom.xml`, todos los getters, setters y constructores están escritos de forma explícita en las entidades para garantizar la portabilidad entre entornos y eliminar problemas con procesadores de anotaciones.
- **Java 21 LTS:** Se descartaron versiones de desarrollo (Java 25) por incompatibilidades del compilador interno (`TypeTag::UNKNOWN`). No se usan características en estado *Preview*.
- **Hibernate `hbm2ddl.auto=update`:** El esquema se sincroniza automáticamente al arranque. Para entornos de producción se recomienda cambiar a `validate` e importar los dumps SQL manualmente.
- **`show_sql=true`:** Habilitado para facilitar la depuración durante el desarrollo. Se recomienda desactivar en producción.