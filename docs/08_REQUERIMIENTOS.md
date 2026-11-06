# NECESIDAD
La UNSC necesita un sistema centralizado que le permita manejar sus ramas, divisiones,
divisiones especiales y reclutas. Ya sea para eliminar, agregar, modificar o consultar
información del sistema. El sistema tiene que gestionar los reclutas y su asignación
a una división especial.

---

# 1. TECNOLOGÍAS POR ÁREA DE LA APLICACIÓN

La selección se basa en la arquitectura del proyecto descrita en los capítulos 00 a 07
(arquitectura simplificada: una vista Vaadin, un servicio CRUD genérico y entidades JPA).

| Área | Tecnología | Versión | Uso dentro del sistema |
| :--- | :--- | :--- | :--- |
| Interfaz de usuario (Frontend) | Vaadin Flow + Java (tema Aura) | 25.3.0 / Java 25 | Construcción de la UI 100% en Java: `TabSheet`, `FormLayout`, `Grid`, `TextField`, `Button`, `Notification`. Sin HTML, JavaScript ni CSS propios. |
| Comunicación en tiempo real | `@Push` de Vaadin (WebSockets) | Incluido en Vaadin | Actualización inmediata de las grillas y notificaciones tras cada operación. |
| Backend | Java + Spring Boot | Java 25 / 4.1.1 | Arranque de la aplicación, inyección de dependencias (`@Service`, `@Autowired`), manejo de transacciones (`@Transactional`) y servidor Tomcat embebido en el puerto 8080. |
| Capa de servicio | `CrudService` genérico sobre `EntityManager` | — | Operaciones universales `guardar`, `buscarPorId`, `listarTodos`, `actualizar` y `eliminar` para todas las entidades, sin interfaces de repositorio. |
| Persistencia / ORM | Hibernate ORM + Jakarta Persistence (JPA) | Gestionadas por `spring-boot-starter-data-jpa` | Mapeo objeto-relacional de las entidades y generación automática del esquema (`ddl-auto=update`). |
| Pool de conexiones | HikariCP | Incluido en Spring Boot | Reutilización y administración de las conexiones JDBC. |
| Base de datos | PostgreSQL (proveedor Neon) | PostgreSQL 16 o superior | Almacenamiento relacional de todas las entidades del sistema. |
| Driver de base de datos | `org.postgresql:postgresql` | `runtime` | Comunicación JDBC entre la aplicación y PostgreSQL. |
| Configuración | `application.properties` + variables de entorno | — | Parámetros de servidor, credenciales y ORM. Variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`. |
| Construcción y dependencias | Apache Maven + Maven Wrapper (`mvnw` / `mvnw.cmd`) | 3.9 o superior | Resolución de dependencias, compilación y empaquetado. Plugins: `spring-boot-maven-plugin` y `vaadin-maven-plugin` (`build-frontend`). |
| Pruebas | JUnit 5 (`spring-boot-starter-test`) y `browserless-test-spring` | Gestionadas por Spring Boot | Pruebas unitarias y de interfaz sin navegador. |
| Entorno de ejecución | JDK 25 (compatible con 21 LTS) y navegador web moderno con WebSocket | — | Ejecución local en `http://localhost:8080` y acceso de los usuarios. |
| Control de versiones | Git | — | Gestión del código fuente y de la documentación del proyecto. |

---

# 2. LIMITACIONES TÉCNICAS

Las limitaciones se derivan de la arquitectura base del proyecto y del alcance definido
en la necesidad.

1. **LT-01 Versiones fijas del stack.** El proyecto queda atado a Java 25, Spring Boot 4.1.1 y Vaadin 25.3.0;

2. **LT-02 Sin migraciones de esquema.** Se usa `spring.jpa.hibernate.ddl-auto=update`; no hay Flyway ni Liquibase. Los cambios de estructura no quedan versionados y no existe ruta de reversión.

3. **LT-03 Sin API REST ni integraciones externas.** La comunicación es propia de Vaadin (servidor-cliente por WebSocket); no se expone una API para aplicaciones móviles, escritorio o terceros.

4. **LT-04 Dependencia exclusiva de PostgreSQL y de Neon.** El dialecto de SQL está fijado a PostgreSQL y la base de datos se hospeda en Neon.

5. **LT-05 Dependencia a internet** El sistema requiere conexión a internet, está sujeto a los límites de conexiones y almacenamiento del plan contratado, y no opera sin conexión.

6. **LT-06 Validación de datos mínima.** No se usa Bean Validation; los valores se convierten manualmente y un dato inválido se transforma en `0`, `null` o se descarta, sin mensajes de error de campo.

7. **LT-07 Borrado físico sin reglas de cascada definidas.** Eliminar una rama, división, división especial o recluta con registros asociados puede fallar por restricciones de clave foránea o dejar información inconsistente.

8. **LT-08 Alcance funcional acotado.** No se contemplan reportes, exportación/importación de archivos, carga de imágenes, notificaciones por correo ni historial de cambios.

9. **LT-09 Interfaz solo en español.** Los formularios usan `FormLayout` estándar; no hay internacionalización ni adaptaciones de usabilidad.

10. **LT-10 Despliegue no automatizado.** La ejecución prevista es local; no se definen contenedores, CI/CD ni monitoreo.

---

# 3. ENTIDADES DEL SISTEMA

Todas las entidades se ubican en el paquete `com.example.model`, se anotan con
`@Entity`, `@Table`, `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)`,
y cuentan con constructor vacío, constructores con y sin ID, y sus getters/setters.

El modelo conserva cinco entidades (`Rama`, `Division`, `DivisionEspecial`, `Recluta` y `AptoDivisionEspecial`).
Las tablas se nombran en plural
mediante `@Table`: `ramas`, `divisiones`, `divisiones_especiales`, `reclutas` y
`aptos_division_especial`.

## 3.1. Entidad `Rama`

Representa cada rama o componente de la UNSC a la que pertenecen las divisiones y los
reclutas.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `idRama` | `Integer` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre de la rama (por ejemplo: Infantería, Armada, Fuerza Aérea). |
| `descripcion` | `String` | Descripción general de la rama. |

Relaciones: una `Rama` tiene muchas `Division` y muchos
`Recluta`. Cada `Division` y cada `Recluta` pertenece a una sola `Rama`.

## 3.2. Entidad `Division`

Unidad organizativa de la UNSC, dependiente de una rama.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `idDivision` | `Integer` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre de la división (por ejemplo: 101.ª Aerotransportada). |
| `descripcion` | `String` | Descripción o propósito de la división. |
| `idRama` | `Rama` | Rama a la que pertenece (`@ManyToOne`, obligatorio). |

Relación: muchas `Division` pertenecen a una `Rama`.

## 3.3. Entidad `DivisionEspecial`

División de la UNSC destinada a reclutas con aptitud especial.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `idDivisionEspecial` | `Integer` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre de la división especial (por ejemplo: ODST, Spartans). |
| `descripcion` | `String` | Descripción o propósito de la división especial. |
| `idDivision` | `Division` | Division a la que pertenece (`@ManyToOne`, obligatorio). |

Relaciones: muchas `DivisionEspecial` pertenecen a una `Division`;

## 3.4. Entidad `Recluta`

Integrante de la UNSC que pertenece a una rama y puede ser asignado a una división
especial.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `idRecluta` | `Integer` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre del recluta. |
| `apellido` | `String` | Apellido del recluta. |
| `altura` | `Integer` | Altura del recluta. |
| `peso` | `BigDecimal` | Peso del recluta. |
| `idRama` | `Rama` | Rama a la que pertenece (`@ManyToOne`, obligatorio). |
| `idApto` | `AptoDivisionEspecial` | Solicitud a Division especial que tiene el soldado (`@OneToOne`, opcional). |

Relaciones: muchos `Recluta` pertenecen a una `Rama`. Un `AptoDivisionEspecial` pertenece a un `Recluta`;

## 3.5. Entidad `AptoDivisionEspecial`

Registro que vincula un `Recluta` con una `DivisionEspecial` y guarda los datos de su
aptitud.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `idApto` | `Integer` | Clave primaria autoincremental. |
| `idRecluta` | `Recluta` | Recluta asignado (`@OneToOne`, obligatorio, `unique`). |
| `idDivisionEspecial` | `DivisionEspecial` | División especial de destino (`@ManyToOne`, obligatorio). |
| `aptoServicio` | `Boolean` | True si paso la evaluación, False si no pasó la evaluación. |
| `fechaEvaluacion` | `LocalDate` | Fecha en la que se realizó la evaluación. |
| `puntaje` | `Integer` | Puntaje obtenido en la evaluación. |

Relaciones: muchos `AptoDivisionEspecial` pertenecen a una `DivisionEspecial` y un `Recluta` tiene un solo `AptoDivisionEspecial`.

Reglas estructurales:

- Un `Recluta` solo puede tener un registro de asignación y el registro no puede existir
  sin `Recluta`.
- Una `DivisionEspecial` puede aparecer en muchos registros de asignación.
- El registro de asignación se consulta, modifica o elimina sin eliminar al `Recluta`.

## 3.6. Resumen de relaciones

```
Rama (1) ──── (N) Division
Division (1) ──── (N) DivisionEspecial
Rama (1) ──── (N) Recluta
Recluta (1) ──── (1) AptoDivisionEspecial        [opcional del lado Recluta]
DivisionEspecial (1) ──── (N) AptoDivisionEspecial
```

Tablas generadas por Hibernate: `ramas`, `divisiones`, `divisiones_especiales`,
`reclutas` y `aptos_division_especial`.

---

# 4. REQUERIMIENTOS FUNCIONALES

## RF-01 Gestión de ramas
El sistema debe permitir crear, consultar, modificar y eliminar ramas de la UNSC,
registrando nombre y descripción, y permitiendo buscarlas por nombre.

## RF-02 Gestión de divisiones
El sistema debe permitir crear, consultar, modificar y eliminar divisiones, asociarlas
a una rama existente y buscarlas por nombre o rama.

## RF-03 Gestión de divisiones especiales
El sistema debe permitir crear, consultar, modificar y eliminar divisiones especiales,
asociarlas a una división existente, mostrar la rama a la que pertenece dicha división y
buscarlas por nombre, división o rama.

## RF-04 Gestión de reclutas
El sistema debe permitir crear, consultar, modificar y eliminar reclutas, registrar sus
datos personales y físicos, asignarles una rama y buscarlos por identificador, nombre,
apellido o rama.

## RF-05 Gestión de asignaciones a divisiones especiales
El sistema debe permitir seleccionar un recluta existente y registrar su asignación a una
división especial, indicando la división especial (que actúa como especialidad), la fecha
de evaluación, el resultado de aptitud y el puntaje obtenido. Cada recluta solo puede
tener una asignación, mientras que una división especial puede tener muchas asignaciones.
La asignación debe poder consultarse, modificarse o retirarse sin eliminar al recluta, y
debe poder buscarse por división especial o estado de aptitud.

## RF-06 Interfaz de usuario
El sistema debe ofrecer una interfaz web implementada con Vaadin Flow, organizada en
pestañas por módulo (Ramas, Divisiones, Divisiones Especiales, Reclutas y Asignaciones a
Divisiones Especiales), con formularios de captura, grillas de consulta, selección de fila
para cargar el formulario y botones de Crear, Consultar, Actualizar, Eliminar y Limpiar.

## RF-07 Persistencia de datos
Toda operación realizada en la interfaz debe almacenarse en la base de datos PostgreSQL
(proveedor Neon) mediante Hibernate ORM, con las tablas creadas y actualizadas
automáticamente a partir de las entidades JPA.

## RF-08 Sincronización frontend-backend
Los cambios efectuados en la interfaz deben reflejarse de inmediato en las grillas y
conservarse después de recargar o reiniciar la aplicación, de modo que la vista muestre
siempre la información almacenada en la base de datos.

## RF-09 Consulta y búsqueda de información
El sistema debe permitir consultar el detalle de cualquier registro por su identificador y
listar los registros de cada módulo, mostrando los datos mínimos definidos en las
entidades.

## RF-10 Integridad y validación básica
El sistema debe validar manualmente los campos obligatorios antes de guardar y notificar
cuando falte alguno, garantizar que cada recluta conserve su identificador único
autogenerado (`idRecluta`) e impedir la eliminación de una rama con divisiones o reclutas
asociados, de una división con divisiones especiales asociadas, de una división especial
con asignaciones o de un recluta con una asignación, informando el motivo al usuario
mediante notificaciones.

## RF-11 Retroalimentación al usuario
El sistema debe notificar el resultado de cada operación (éxito, registro no encontrado o
error de validación) y limpiar el formulario cuando corresponda.

---

# 5. REGLAS DE NEGOCIO PENDIENTES DE DEFINIR

Los siguientes aspectos no se especifican en la necesidad y deben acordarse antes de su
implementación:

- Cupos máximos de reclutas por rama y requisitos para pertenecer a una división especial.
- Criterios de evaluación, puntaje mínimo y vigencia de la aptitud para asignar un recluta
  a una división especial.
- Política de borrado (físico o lógico) y reasignación de reclutas al eliminar una rama o
  una división especial.
- Necesidad real de autenticación, roles y permisos sobre el sistema.
