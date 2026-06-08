# Registro de Uso Responsable y Transparente de Inteligencia Artificial

Este documento cumple con las directrices académicas y profesionales de transparencia, recopilando de forma estructurada los *prompts* y consultas realizados a herramientas de Inteligencia Artificial como apoyo técnico durante el desarrollo del sistema **Aulateca**.

El uso de la IA en este proyecto se ha enfocado de manera crítica y responsable, empleándose exclusivamente como un asistente avanzado para la resolución de errores del compilador, la optimización de algoritmos de persistencia y la generación formal de la documentación técnica.

---

## Registro de Prompts de Configuración y Resolución de Errores

### Consulta 1: Resolución del bug crítico de compilación en el entorno local
> "Al intentar ejecutar el método main de la interfaz gráfica en IntelliJ IDEA tras modificar las dependencias del pom.xml, el compilador lanza una excepción crítica de inicialización: java.lang.ExceptionInInitializerError en com.sun.tools.javac.code.TypeTag::UNKNOWN. El proceso de compilación queda bloqueado por completo. ¿Cuál es el origen de este error y cómo se puede resolver?"

### Consulta 2: Resolución del desfase de versiones del SDK del Módulo
> "Tras aplicar la corrección anterior y establecer Java 21 como versión objetivo, el compilador ahora lanza el siguiente error: error: release version 25 not supported. Module Aulateca SDK 21 is not compatible with the source version 25. ¿En qué puntos de la configuración del proyecto y del pom.xml puede existir un desajuste de versiones y cómo se debe corregir de forma consistente?"

---

## Registro de Prompts de Arquitectura y Lógica de Datos

### Consulta 3: Planteamiento conceptual y rendimiento de relaciones ORM
> "En el modelo de entidades JPA del proyecto, necesito mapear relaciones entre las tablas maestras de recursos y las reservas. ¿Cuál es la estrategia arquitectónica más eficiente en términos de memoria y rendimiento: utilizar relaciones bidireccionales con colecciones @OneToMany o implementar relaciones unidireccionales @ManyToOne? ¿Qué tipo de FetchType sería el más adecuado en este contexto?"

### Consulta 4: Validación de reglas de negocio sobre mantenimiento y solapamiento horario
> la prevención de reservas con solapamiento horario y el bloqueo de recursos marcados como inactivos o en mantenimiento. ¿Cómo se debería estructurar esta lógica defensiva y qué tipo de consulta HQL sería la más adecuada en el ReservationDAO?"

---

## Registro de Prompts de Control de Versiones (Git)

### Consulta 5: Resolución de conflictos de colisión de repositorios remotos
> "Al configurar el repositorio remoto en Git, se asignó por error la URL de otro proyecto mediante git remote add origin. Al intentar sobrescribirla con la URL correcta, el sistema indica que el origen ya existe. ¿Cuál es el comando adecuado para reemplazar la URL remota incorrecta y cómo se puede verificar posteriormente que la configuración ha quedado correctamente establecida?"

---

## Registro de Prompts de Interfaz Gráfica (Swing / UI)

### Consulta 6: Instalar la librería del calendario (LGoodDatePicker)
> "Quiero usar un calendario visual en mi proyecto en lugar de escribir la fecha a mano. He visto que la librería LGoodDatePicker está muy bien. Como mi proyecto de IntelliJ usa Maven, ¿cómo tengo que poner la dependencia en el archivo pom.xml y qué código básico necesito para que el calendario aparezca en un JPanel?"

### Consulta 7: Desvincular el estilo nativo de los botones
> "Tengo un problema visual. Le estoy poniendo color de fondo azul a mis botones con setBackground(), pero cuando ejecuto el programa en Windows o Mac, siguen saliendo con el estilo gris por defecto del sistema operativo. ¿Cómo desvinculo el estilo nativo del sistema para que los botones respeten los colores y el diseño que yo les pongo?"

### Consulta 8: Bloquear días concretos en el calendario
> "En la ventana de reservas, necesito que el usuario no pueda seleccionar los fines de semana ni los días que ya han pasado. He leído en la documentación de LGoodDatePicker que se le puede pasar una política de veto (veto policy). ¿Me ayudas a escribir ese trozo de código para desactivar esos días concretos?"

### Consulta 9: Asistente de reservas sin abrir nuevas ventanas
> "Para hacer una nueva reserva el usuario tiene que pasar por 3 pasos (elegir tipo, elegir recurso, elegir fecha). No quiero que se vayan abriendo y cerrando ventanas todo el rato, me gustaría que fuera como un asistente donde el contenido cambia dentro del mismo recuadro. ¿Qué puedo usar para esto?"

### Consulta 10: Ocultar la columna ID en el JTable
> "Estoy mostrando los usuarios en un JTable, pero queda muy feo que la primera columna sea el ID de la base de datos. El problema es que necesito tener ese ID a mano en la tabla para cuando le den al botón de borrar. ¿Hay alguna forma o truco en Swing para hacer que la columna del ID no se vea visualmente pero yo siga pudiendo leer el dato?"

### Consulta 11: Mostrar nombres limpios en el JComboBox
> "Para no perder la referencia, estoy metiendo objetos enteros dentro de los JComboBox (por ejemplo, el objeto ResourceType). Pero al abrir el desplegable, en pantalla sale un texto feísimo como aulateca.model.ResourceType@45a1b. ¿Cómo le digo al combobox que solo renderice el atributo nombre del objeto para que quede limpio?"

### Consulta 12: Poner en rojo los días sin huecos (DateHighlightPolicy)
> "Quiero mejorar el calendario. Ya tengo una lista con las fechas que están totalmente llenas porque se han ocupado todas sus franjas horarias. ¿Cómo puedo enlazar esa lista con el calendario para que pinte el fondo de esos días en color rojo y así el usuario vea rápido que están ocupados?"

---

## Registro de Prompts de Lógica de Negocio y Persistencia

### Consulta 13: Filtros dinámicos usando Streams
> "Tengo una lista con todas las reservas cargadas de la base de datos. En la interfaz tengo tres filtros (fecha, usuario y recurso). Necesito hacer un método que filtre esa lista en memoria basándose en lo que el usuario haya seleccionado en esos tres desplegables. He visto que se puede hacer rápido usando Streams en Java 8, ¿me ayudas con la lógica?"

### Consulta 14: Control de excepciones para que no explote el programa
> "En la rúbrica me piden que la aplicación no se cierre de golpe si hay errores de conexión. Tengo bastantes métodos que leen de la base de datos al arrancar las ventanas (findAll). ¿Cómo debería envolver esos trozos de código con try-catch para que, si el servidor falla, el programa no colapse y simplemente muestre un mensaje con un JOptionPane?"

### Consulta 15: Lógica de la contraseña al actualizar un usuario
> "Estoy haciendo el botón de actualizar usuario. Si el administrador deja el campo de la contraseña vacío, quiero que se mantenga la contraseña vieja que ya estaba en la base de datos, pero si escribe algo en ese campo, entonces sí quiero que se sobrescriba. ¿Cómo planteo esos if antes de mandar el update al DAO?"