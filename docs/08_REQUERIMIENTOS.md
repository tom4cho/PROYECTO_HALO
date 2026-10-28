# NECESIDAD
La UNSC necesita un sistema centralizado que le permita manejar sus divisiones,
ramas, soldados y armamento. Ya sea para eliminar, agregar, modificar o consultar 
información del sistema. El sistema tiene que gestionar sus soldados generales y soldados 
aptos para divisiones especiales ( Estos ultimos vienen desde soldados ).

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

2. **LT-02 Servicio CRUD sin consultas personalizadas.** `CrudService` Se excluyen consultas JPQL/HQL. Especializadas.

4. **LT-04 Sin migraciones de esquema.** Se usa `spring.jpa.hibernate.ddl-auto=update`; no hay Flyway ni Liquibase. Los cambios de estructura no quedan versionados y no existe ruta de reversión.

2. **LT-06 Sin API REST ni integraciones externas.** La comunicación es propia de Vaadin (servidor-cliente por WebSocket); no se expone una API para aplicaciones móviles, escritorio o terceros.

7. **LT-07 Dependencia exclusiva de PostgreSQL y de Neon.** El dialecto de SQL está fijado a PostgreSQL y la base de datos se hospeda en Neon.

8. **LT-07 Dependencia a internet** El sistema requiere conexión a internet, está sujeto a los límites de conexiones y almacenamiento del plan contratado, y no opera sin conexión.

8. **LT-08 Validación de datos mínima.** No se usa Bean Validation; los valores se convierten manualmente y un dato inválido se transforma en `0`, `null` o se descarta, sin mensajes de error de campo.

11. **LT-11 Borrado físico sin reglas de cascada definidas.** Eliminar una rama, división o soldado con registros asociados puede fallar por restricciones de clave foránea o dejar información inconsistente.

13. **LT-13 Alcance funcional acotado.** No se contemplan reportes, exportación/importación de archivos, carga de imágenes, notificaciones por correo ni historial de cambios.

14. **LT-14 Interfaz solo en español.** Los formularios usan `FormLayout` estándar; no hay internacionalización ni adaptaciones de usabilidad.

15. **LT-15 Despliegue no automatizado.** La ejecución prevista es local; no se definen contenedores, CI/CD ni monitoreo.

---

# 3. ENTIDADES DEL SISTEMA

Todas las entidades se ubican en el paquete `com.example.model`, se anotan con
`@Entity`, `@Table`, `@Id` y `@GeneratedValue(strategy = GenerationType.IDENTITY)`,
y cuentan con constructor vacío, constructores con y sin ID, y sus getters/setters.

## 3.1. Entidad `Rama`

Representa cada rama o componente de la UNSC a la que pertenecen las divisiones.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre de la rama (por ejemplo: Infantería, Armada, Fuerza Aérea). |
| `descripcion` | `String` | Descripción general de la rama. |
| `estado` | `String` | `Activa` o `Inactiva`. |

Relación: una `Rama` tiene muchas `Division`.

## 3.2. Entidad `Division`

Unidad organizativa de la UNSC, dependiente de una rama.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre de la división (por ejemplo: 101.ª Aerotransportada). |
| `tipo` | `String` | Tipo de división (Infantería, Blindada, Aérea, Reconocimiento, Especial). |
| `descripcion` | `String` | Descripción o propósito de la división. |
| `estado` | `String` | `Activa`, `En formación` o `Disuelta`. |
| `rama` | `Rama` | Rama a la que pertenece (`@ManyToOne`). |

Relaciones: muchas `Division` pertenecen a una `Rama`; una `Division` agrupa muchos
`Soldado` y puede tener asignados varios `Armamento`.

## 3.3. Entidad `Soldado`

Soldado general de la UNSC. Es la entidad base de los soldados especiales.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `codigo` | `String` | Identificación única del soldado (por ejemplo: UNSC-00125). |
| `nombre` | `String` | Nombre del soldado. |
| `apellido` | `String` | Apellido del soldado. |
| `rango` | `String` | Rango militar (Recluta, Sargento, Teniente, Capitán, etc.). |
| `estado` | `String` | `Activo`, `Inactivo` o `Baja`. |
| `fechaIngreso` | `LocalDate` | Fecha de ingreso a la UNSC. |
| `division` | `Division` | División asignada (`@ManyToOne`, opcional). Si es `null`, el soldado es general y no está asignado a una división. |

Relación: muchos `Soldado` pertenecen a una `Division`.

## 3.4. Entidad `SoldadoEspecial`

Soldado apto para divisiones especiales. Hereda de `Soldado` (estrategia `JOINED`),
de modo que todo soldado especial conserva los datos del soldado del que proviene y
existe como un único registro en el sistema.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria y clave foránea hacia `Soldado`. |
| `especialidad` | `String` | Especialidad (por ejemplo: ODST, Spartans, Fuerzas Especiales). |
| `fechaAptitud` | `LocalDate` | Fecha en la que se declaró apto. |
| `estadoAptitud` | `String` | `Apto`, `En evaluación` o `No apto`. |
| `divisionEspecial` | `Division` | División especial a la que fue destinado (`@ManyToOne`). |

Regla estructural: un `SoldadoEspecial` solo puede registrarse a partir de un `Soldado`
previamente existente; no se crea desde cero.

## 3.5. Entidad `Armamento`

Equipo y armamento de la UNSC disponible para las divisiones.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `codigo` | `String` | Identificación única del armamento. |
| `nombre` | `String` | Nombre del armamento (por ejemplo: MA5B Assault Rifle). |
| `categoria` | `String` | Categoría (Fusil, Pistola, Explosivo, Artillería, Vehículo). |
| `calibre` | `String` | Calibre o especificación técnica principal. |
| `descripcion` | `String` | Especificaciones y observaciones. |
| `estado` | `String` | `Operativo`, `En mantenimiento` o `Fuera de servicio`. |
| `cantidadDisponible` | `Integer` | Unidades disponibles en inventario. |
| `division` | `Division` | División a la que está asignado (`@ManyToOne`, opcional). |

Relación: muchos `Armamento` pueden estar asignados a una `Division`.

## 3.6. Entidad de apoyo opcional `Usuario`

No forma parte del alcance mínimo de la necesidad, pero la arquitectura base ya la
contempla y permitiría identificar al personal que opera el sistema.

| Campo | Tipo Java | Descripción |
| :--- | :--- | :--- |
| `id` | `Long` | Clave primaria autoincremental. |
| `nombre` | `String` | Nombre del usuario. |
| `apellido` | `String` | Apellido del usuario. |
| `email` | `String` | Correo de contacto. |
| `rol` | `String` | `Administrador` u `Operador`. |
| `estado` | `String` | `Activo` o `Inactivo`. |
| `fechaRegistro` | `LocalDateTime` | Fecha y hora de creación del usuario. |

## 3.7. Resumen de relaciones

```
Rama (1) ──── (N) Division
Division (1) ──── (N) Soldado
Division (1) ──── (N) Armamento
Soldado (1) ──── (1) SoldadoEspecial        [herencia JOINED]
Division (1) ──── (N) SoldadoEspecial       [divisionEspecial]
```

Tablas generadas por Hibernate: `ramas`, `divisiones`, `soldados`,
`soldados_especiales`, `armamentos` y, si se implementa, `usuarios`.

---

# 4. REQUERIMIENTOS FUNCIONALES

## RF-01 Gestión de ramas
El sistema debe permitir crear, consultar, modificar y eliminar ramas de la UNSC,
registrando nombre, descripción y estado, y permitiendo buscarlas por nombre.

## RF-02 Gestión de divisiones
El sistema debe permitir crear, consultar, modificar y eliminar divisiones, asociarlas
a una rama existente y buscarlas por nombre, tipo, estado o rama.

## RF-03 Gestión de soldados generales
El sistema debe permitir crear, consultar, modificar y eliminar soldados, registrar sus
datos personales y militares, asignarlos o desasignarlos de una división, y buscarlos por
código, nombre, apellido, rango, estado o división.

## RF-04 Gestión de soldados aptos para divisiones especiales
El sistema debe permitir seleccionar un soldado existente y registrar su aptitud para una
división especial, indicando especialidad, fecha de aptitud, estado de aptitud y división
especial de destino. El soldado especial debe poder consultarse, modificarse y retirarse
de la lista de aptos sin eliminar al soldado original, y debe poder buscarse por
especialidad, estado de aptitud o división especial.

## RF-05 Gestión de armamento
El sistema debe permitir crear, consultar, modificar y eliminar armamento, registrar su
categoría, calibre, estado y cantidad disponible, asignarlo a una división y buscarlo por
código, nombre, categoría, estado o división.

## RF-06 Interfaz de usuario
El sistema debe ofrecer una interfaz web implementada con Vaadin Flow, organizada en
pestañas por módulo (Ramas, Divisiones, Soldados, Soldados Especiales y Armamento), con
formularios de captura, grillas de consulta, selección de fila para cargar el formulario y
botones de Crear, Consultar, Actualizar, Eliminar y Limpiar.

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
El sistema debe validar los campos obligatorios antes de guardar, impedir códigos
duplicados en soldados y armamento, e impedir la eliminación de una rama con divisiones
asociadas o de una división con soldados o armamento asignados, informando el motivo al
usuario mediante notificaciones.

## RF-11 Retroalimentación al usuario
El sistema debe notificar el resultado de cada operación (éxito, registro no encontrado o
error de validación) y limpiar el formulario cuando corresponda.

---

# 5. REGLAS DE NEGOCIO PENDIENTES DE DEFINIR

Los siguientes aspectos no se especifican en la necesidad y deben acordarse antes de su
implementación:

- Rangos militares válidos y jerarquía entre ellos.
- Cupos máximos de soldados por división y requisitos para pertenecer a una división especial.
- Tipos de división y de armamento admitidos.
- Política de borrado (físico o lógico) y reasignación de soldados al eliminar una división.
- Tratamiento de soldados dados de baja y de armamento fuera de servicio.
- Necesidad real de autenticación, roles y permisos sobre el sistema.
