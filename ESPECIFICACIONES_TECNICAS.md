# Especificaciones Técnicas - Sistema Aulateca

Este documento describe la arquitectura de software, las decisiones tecnológicas adoptadas y la configuración del entorno técnico del sistema **Aulateca**.

## 1. Estructura y Organización en Capas (Arquitectura Desacoplada)
La aplicación sigue estrictamente el patrón arquitectónico **Modelo-Vista-Controlador (MVC)** suplementado con una **Capa de Servicio Intermedia**. Esta separación de responsabilidades asegura la mantenibilidad y escalabilidad del código. Los paquetes principales del código fuente se estructuran dentro de `src/main/java/com/aulateca` de la siguiente forma:

* `com.aulateca.model`: Aloja las entidades de datos (POJOs planos) anotadas con JPA que representan la estructura de la base de datos relacional.
* `com.aulateca.repository`: Contiene las clases encargadas de la persistencia de datos (Data Access Objects - DAO). Aquí se aísla por completo el código de Hibernate.
* `com.aulateca.service`: La capa intermedia o "cerebro" del software. Implementa la lógica comercial del negocio, aplicando las validaciones pertinentes.
* `com.aulateca.view`: La capa de presentación y visualización, desarrollada sobre el framework nativo **Java Swing**. Captura los eventos del usuario y los redirige hacia los servicios.

## 2. Tecnologías Utilizadas y Decisiones Adoptadas
* **Java 21 Estándar (LTS):** Se utiliza la última versión de soporte a largo plazo de Java para aprovechar estructuras modernas de control de flujo. Se descartó el uso de características *Preview* (como los String Templates) o versiones de desarrollo inestables (Java 25) debido a bugs de incompatibilidad detectados en el compilador `javac` interno (`TypeTag :: UNKNOWN`).
* **Maven:** Gestor de construcción del proyecto que centraliza las dependencias externas a través del archivo raíz `pom.xml`.
* **Hibernate 6.x / Jakarta Persistence (JPA):** Motor de mapeo objeto-relacional (ORM) para la abstracción de consultas SQL.
* **MySQL / MariaDB:** Sistema gestor de bases de datos relacionales utilizado para el almacenamiento persistente.
* **Java Swing Nativo:** Utilizado para la interfaz gráfica. Los componentes visuales se maquetan dinámicamente mediante código puro utilizando administradores de diseño avanzados como **`GridBagLayout`**, asegurando un posicionamiento simétrico y elástico sin depender de generadores visuales de arrastrar y soltar.

## 3. Configuración de Hibernate y Conexión de Datos
La comunicación con la base de datos MySQL se gestiona mediante el archivo de configuración estándar de JPA/Hibernate. Las propiedades de conexión establecen parámetros estrictos de rendimiento y seguridad:

* **Estrategia de Carga Eficiente (`@ManyToOne`):** Para evitar el problema clásico de persistencia de las consultas masivas $N+1$ asociadas al uso desmedido de colecciones `@OneToMany`, se optó por un enfoque estrictamente **unidireccional Muchos a Uno** en las entidades hijas. En la clase `Resource`, estas uniones se parametrizan con **`FetchType.EAGER`**, lo que instruye a Hibernate a recuperar el recurso, su tipo y su estado operativo en un único `JOIN` optimizado de SQL, agilizando su renderizado inmediato en las tablas gráficas de Swing.
* **Gestión Segura de Recursos (Patrón Repositorio Genérico):** La clase base `GenericDAO<T>` implementa operaciones CRUD reutilizables mediante bloques **`try-with-resources`** de Java. Esto garantiza de forma automatizada que las sesiones de comunicación con la base de datos (`Session`) se cierren adecuadamente tras finalizar una consulta, eliminando de raíz el riesgo de fugas de conexiones (*connection leaks*). Las escrituras se protegen mediante transacciones atómicas que ejecutan `.rollback()` si la base de datos reporta anomalías.

## 4. Estabilidad del Entorno: Eliminación de Lombok
Durante la fase inicial de desarrollo, se experimentaron bloqueos de compilación recurrentes relacionados con la inyección de código en caliente en el entorno local. Tras realizar el diagnóstico técnico, se tomó la decisión de **eliminar por completo la librería Lombok** de las dependencias de Maven. En su lugar, el modelo declara explícitamente sus métodos de acceso (*Getters*, *Setters* y constructores), eliminando dependencias de procesadores de anotaciones en segundo plano y blindando la portabilidad del repositorio entre diferentes entornos de desarrollo e IDEs de forma limpia.