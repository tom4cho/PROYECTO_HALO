# Log de generación de datos — `docs/DATA.md`

- **Fecha de ejecución:** 2026-11-11
- **Base de datos:** Neon PostgreSQL 18.6 — `neondb` (schema `public`)
- **Aplicación:** Spring Boot 4.1.1 + Vaadin 25.3.0, iniciada con `spring-boot:run` en `http://localhost:8081` (el puerto 8080 estaba ocupado por otro proceso). Hibernate creó/actualizó las cinco tablas con `ddl-auto=update`.
- **Método de carga:** SQL directo vía JDBC (`postgresql-42.7.13.jar`) con `jshell`, contra las restricciones reales de PostgreSQL. Los `INSERT` usan `RETURNING` para registrar el ID generado.
- **Estado inicial:** las cinco tablas estaban vacías (0 registros).

---

## 1. Resumen final

| Tabla | Solicitado | Creado | Estado |
|---|---|---|---|
| `ramas` | 4 | 4 | ✅ |
| `divisiones` | 2 por rama = 8 | 8 | ✅ |
| `divisiones_especiales` | 2 por división = 16 | 16 | ✅ |
| `reclutas` | 20 | 20 | ✅ |
| `aptos_division_especial` | 10 | 10 | ✅ |

Casos inválidos solicitados: **14** (2 + 2 + 2 + 3 + 5) → **14 rechazados** por la base de datos, **0 registros creados**, por lo que **no hubo nada que eliminar**.

---

## 2. Registros válidos creados

### 2.1 `ramas` (4)

| id_rama | nombre | descripción |
|---|---|---|
| 4 | Infantería | Fuerza terrestre principal de la UNSC. |
| 5 | Armada | Flota naval y aeroespacial de la UNSC. |
| 6 | Fuerza Aérea | Aviación táctica y transporte de la UNSC. |
| 7 | Cuerpo de Marines | Tropas expedicionarias de la UNSC. |

### 2.2 `divisiones` (2 por rama = 8)

| id_division | nombre | id_rama | rama |
|---|---|---|---|
| 1 | 101.ª Aerotransportada | 4 | Infantería |
| 2 | 3.ª División de Infantería | 4 | Infantería |
| 3 | Flota del Sol | 5 | Armada |
| 4 | Escuadrón de Exploración | 5 | Armada |
| 5 | Ala de Caza 77 | 6 | Fuerza Aérea |
| 6 | Grupo de Transporte 12 | 6 | Fuerza Aérea |
| 7 | 1.ª División de Marines | 7 | Cuerpo de Marines |
| 8 | División de Operaciones Especiales | 7 | Cuerpo de Marines |

### 2.3 `divisiones_especiales` (2 por división = 16)

| id_division_especial | nombre | id_division | división |
|---|---|---|---|
| 1 | ODST | 1 | 101.ª Aerotransportada |
| 2 | Spartans | 1 | 101.ª Aerotransportada |
| 3 | Reconocimiento | 2 | 3.ª División de Infantería |
| 4 | Asalto Orbital | 2 | 3.ª División de Infantería |
| 5 | Fuerzas Especiales Navales | 3 | Flota del Sol |
| 6 | Buceadores de Combate | 3 | Flota del Sol |
| 7 | Exploradores | 4 | Escuadrón de Exploración |
| 8 | Interdicción | 4 | Escuadrón de Exploración |
| 9 | Escuadrón Halcón | 5 | Ala de Caza 77 |
| 10 | Tropas Aeromóviles | 5 | Ala de Caza 77 |
| 11 | Logística Avanzada | 6 | Grupo de Transporte 12 |
| 12 | Evacuación Médica | 6 | Grupo de Transporte 12 |
| 13 | Fuerza de Reacción Rápida | 7 | 1.ª División de Marines |
| 14 | Asalto Planetario | 7 | 1.ª División de Marines |
| 15 | Comandos | 8 | División de Operaciones Especiales |
| 16 | Operaciones Encubiertas | 8 | División de Operaciones Especiales |

### 2.4 `reclutas` (20 — 5 por rama)

| id_recluta | nombre | apellido | altura | peso | id_rama | rama |
|---|---|---|---|---|---|---|
| 1 | John | Shepard | 188 | 82.500 | 4 | Infantería |
| 2 | Jane | Cortez | 175 | 68.200 | 4 | Infantería |
| 3 | Marcus | Reyes | 182 | 79.400 | 4 | Infantería |
| 4 | Elena | Vasquez | 170 | 63.800 | 4 | Infantería |
| 5 | David | Chen | 178 | 74.100 | 5 | Armada |
| 6 | Sarah | Connor | 172 | 66.500 | 5 | Armada |
| 7 | Miguel | Alvarez | 185 | 80.300 | 5 | Armada |
| 8 | Aisha | Rahman | 168 | 60.700 | 5 | Armada |
| 9 | Thomas | Kirk | 190 | 85.600 | 6 | Fuerza Aérea |
| 10 | Laura | Palmer | 165 | 58.900 | 6 | Fuerza Aérea |
| 11 | Victor | Stone | 195 | 92.100 | 6 | Fuerza Aérea |
| 12 | Nina | Williams | 173 | 64.400 | 6 | Fuerza Aérea |
| 13 | Carlos | Mendez | 180 | 77.800 | 7 | Cuerpo de Marines |
| 14 | Grace | Hopper | 167 | 61.200 | 7 | Cuerpo de Marines |
| 15 | Samuel | Carter | 186 | 83.700 | 7 | Cuerpo de Marines |
| 16 | Hana | Sato | 169 | 59.500 | 7 | Cuerpo de Marines |
| 17 | Omar | Hassan | 191 | 88.900 | 4 | Infantería |
| 18 | Isabelle | Laurent | 174 | 65.600 | 5 | Armada |
| 19 | Ethan | Hunt | 183 | 78.400 | 6 | Fuerza Aérea |
| 20 | Zoe | Walker | 171 | 62.300 | 7 | Cuerpo de Marines |

### 2.5 `aptos_division_especial` (10)

| id_apto | recluta | división especial | apto_servicio | fecha_evaluacion | puntaje |
|---|---|---|---|---|---|
| 1 | John Shepard | ODST | true | 2026-01-10 | 95 |
| 2 | Jane Cortez | Spartans | true | 2026-01-11 | 88 |
| 3 | Marcus Reyes | Reconocimiento | false | 2026-01-12 | 55 |
| 4 | Elena Vasquez | Asalto Orbital | true | 2026-01-13 | 91 |
| 5 | David Chen | Fuerzas Especiales Navales | true | 2026-01-14 | 84 |
| 6 | Sarah Connor | Buceadores de Combate | false | 2026-01-15 | 49 |
| 7 | Miguel Alvarez | Exploradores | true | 2026-01-16 | 90 |
| 8 | Aisha Rahman | Interdicción | true | 2026-01-17 | 76 |
| 9 | Thomas Kirk | Escuadrón Halcón | false | 2026-01-18 | 58 |
| 10 | Laura Palmer | Tropas Aeromóviles | true | 2026-01-19 | 87 |

Los reclutas 11–20 no tienen apto asignado (relación 1:1 opcional, 10 de 20 reclutas asignados).

---

## 3. Casos inválidos (todos rechazados — 0 registros creados)

Ningún caso se creó, por lo que no fue necesario eliminar filas. Los mensajes son los devueltos por PostgreSQL.

### `ramas` — 2 casos

| # | Caso | Resultado | SQLSTATE | Error |
|---|---|---|---|---|
| 1 | `id_rama` duplicado (PK = 4) | RECHAZADO | 23505 | `duplicate key value violates unique constraint "ramas_pkey"` — `Key (id_rama)=(4) already exists` |
| 2 | `nombre` de 300 caracteres (límite 255) | RECHAZADO | 22001 | `value too long for type character varying(255)` |

> Observación: `ramas.nombre` es `NOT NULL` en la entidad/BD, por lo que ambos casos se resolvieron con violación de PK y de longitud, que sí son restricciones reales.

### `divisiones` — 2 casos

| # | Caso | Resultado | SQLSTATE | Error |
|---|---|---|---|---|
| 1 | `id_rama` NULL | RECHAZADO | 23502 | `null value in column "id_rama" ... violates not-null constraint` |
| 2 | `id_rama` inexistente (999999) | RECHAZADO | 23503 | `violates foreign key constraint "fkkwc8ywx7r49ygjlwmoa74xhq6"` — `Key (id_rama)=(999999) is not present in table "ramas"` |

### `divisiones_especiales` — 2 casos

| # | Caso | Resultado | SQLSTATE | Error |
|---|---|---|---|---|
| 1 | `id_division` NULL | RECHAZADO | 23502 | `null value in column "id_division" ... violates not-null constraint` |
| 2 | `id_division` inexistente (999999) | RECHAZADO | 23503 | `violates foreign key constraint "fkhkd8csap94uomqmt36xyidhh0"` — `Key (id_division)=(999999) is not present in table "divisiones"` |

### `reclutas` — 3 casos

| # | Caso | Resultado | SQLSTATE | Error |
|---|---|---|---|---|
| 1 | `id_rama` NULL | RECHAZADO | 23502 | `null value in column "id_rama" ... violates not-null constraint` |
| 2 | `id_rama` inexistente (999999) | RECHAZADO | 23503 | `violates foreign key constraint "fk3fj52akvb05pw8qatof4krslv"` — `Key (id_rama)=(999999) is not present in table "ramas"` |
| 3 | `peso` fuera de precisión (`12345.678`, `DECIMAL(6,3)`) | RECHAZADO | 22003 | `numeric field overflow` — `A field with precision 6, scale 3 must round to an absolute value less than 10^3` |

### `aptos_division_especial` — 5 casos

| # | Caso | Resultado | SQLSTATE | Error |
|---|---|---|---|---|
| 1 | `id_recluta` NULL | RECHAZADO | 23502 | `null value in column "id_recluta" ... violates not-null constraint` |
| 2 | `id_division_especial` NULL | RECHAZADO | 23502 | `null value in column "id_division_especial" ... violates not-null constraint` |
| 3 | `id_recluta` inexistente (999999) | RECHAZADO | 23503 | `violates foreign key constraint "fkallawjj5b8duyld6vm37eg7g0"` — `Key (id_recluta)=(999999) is not present in table "reclutas"` |
| 4 | `id_division_especial` inexistente (999999) | RECHAZADO | 23503 | `violates foreign key constraint "fkkyytgafi3193qajibpgpt8dpm"` — `Key (id_division_especial)=(999999) is not present in table "divisiones_especiales"` |
| 5 | recluta ya asignado (repite `id_recluta` = 1, restricción UNIQUE 1:1) | RECHAZADO | 23505 | `duplicate key value violates unique constraint "uk67r088b6wvhrar1v4hllpe2sp"` — `Key (id_recluta)=(1) already exists` |

---

## 4. Verificación de integridad posterior

Distribuciones comprobadas:

- Cada rama tiene exactamente **2 divisiones** (4/4 ramas correctas).
- Cada división tiene exactamente **2 divisiones especiales** (8/8 divisiones correctas).
- Reclutas por rama: Infantería 5, Armada 5, Fuerza Aérea 5, Cuerpo de Marines 5.
- Aptos: 10 registros, todos con recluta y división especial válidos y sin duplicar recluta.

Búsqueda de residuos de los casos inválidos:

```
ramas=0  divisiones=0  especiales=0  reclutas=0  aptos=0
```

(Se buscaron nombres `Duplicada`, `Sin rama`, `Rama inexistente`, `Sin division`, `Division inexistente`, `Sin`/`Rama`/`Peso`, nombres de más de 255 caracteres y aptos con fecha `2026-02-01`.)

Conteos finales:

```
ramas=4
divisiones=8
divisiones_especiales=16
reclutas=20
aptos_division_especial=10
```

---

## 5. Incidencias durante la ejecución

1. **Puerto 8080 ocupado:** el primer arranque de la aplicación falló con `Web server failed to start. Port 8080 was already in use`. Se relanzó con `PORT=8081` y arrancó correctamente (HTTP 200).
2. **Primer script de carga descartado:** un intento inicial de ejecutar todo el seeding en un único bloque `jshell` falló por ambigüedad de `Date` (`java.util.Date` vs `java.sql.Date`) y por el tamaño del script heredoc. **No insertó ningún registro** (conteos verificados en 0 antes de continuar). Se repitió la carga por tablas, en pasos separados y con `Date.valueOf` sin ambigüedad.
3. **Tablas legacy:** la base sigue conteniendo tablas del inventario antiguo y tablas en mayúsculas (`RAMAS`, `DIVISIONES`, etc.) creadas previamente. Solo se usaron las tablas en minúsculas del modelo actual (`ramas`, `divisiones`, `divisiones_especiales`, `reclutas`, `aptos_division_especial`). No se modificó ni eliminó nada de las tablas legacy.
