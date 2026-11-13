package com.example.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.example.model.AptoDivisionEspecial;
import com.example.model.DivisionEspecial;
import com.example.model.Rama;
import com.example.model.Recluta;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio genérico que interactúa directamente con el ORM Hibernate (vía JPA EntityManager)
 * sin necesidad de crear interfaces de Repositorios para cada entidad.
 */
@Service
@Transactional
public class CrudService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Crear / Guardar una nueva entidad en la base de datos
     */
    public <T> T guardar(T entidad) {
        entityManager.persist(entidad);
        return entidad;
    }

    /**
     * Consultar una entidad por su ID
     */
    @Transactional(readOnly = true)
    public <T> T buscarPorId(Class<T> clase, Serializable id) {
        return entityManager.find(clase, id);
    }

    /**
     * Obtener todos los registros de una entidad
     */
    @Transactional(readOnly = true)
    public <T> List<T> listarTodos(Class<T> clase) {
        String hql = "SELECT e FROM " + clase.getSimpleName() + " e";
        return entityManager.createQuery(hql, clase).getResultList();
    }

    /**
     * Actualizar una entidad existente
     */
    public <T> T actualizar(T entidad) {
        return entityManager.merge(entidad);
    }

    /**
     * Eliminar una entidad por su ID
     */
    public <T> boolean eliminar(Class<T> clase, Serializable id) {
        T entidad = entityManager.find(clase, id);
        if (entidad != null) {
            entityManager.remove(entidad);
            return true;
        }
        return false;
    }

    // ==========================================
    // Altas de aptos (consistencia de rama)
    // ==========================================

    /**
     * Crear / Dar de alta un apto de división especial, validando la consistencia
     * de rama antes de persistir dentro de la misma transacción.
     */
    @Transactional
    public AptoDivisionEspecial crearApto(Integer idRecluta, Integer idDivisionEspecial, Boolean aptoServicio,
            LocalDate fechaEvaluacion, Integer puntaje) {
        Recluta recluta = entityManager.find(Recluta.class, idRecluta);
        DivisionEspecial divisionEspecial = entityManager.find(DivisionEspecial.class, idDivisionEspecial);
        if (recluta == null || divisionEspecial == null) {
            throw new ReglaRamaException("No encontrado");
        }
        Rama ramaRecluta = recluta.getIdRama();
        Rama ramaDivision = divisionEspecial.getIdDivision().getIdRama();
        if (!Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())) {
            throw new ReglaRamaException(
                    "El recluta debe pertenecer a la misma rama que la división de la división especial seleccionada.");
        }
        AptoDivisionEspecial apto = new AptoDivisionEspecial(recluta, divisionEspecial, aptoServicio,
                fechaEvaluacion, puntaje);
        entityManager.persist(apto);
        return apto;
    }

    // ==========================================
    // Actualizaciones de aptos (consistencia de rama)
    // ==========================================

    /**
     * Actualizar / Editar un apto de división especial, validando la consistencia
     * de rama antes de merge dentro de la misma transacción.
     */
    @Transactional
    public AptoDivisionEspecial actualizarApto(Integer idApto, Integer idRecluta, Integer idDivisionEspecial,
            Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje) {
        AptoDivisionEspecial apto = entityManager.find(AptoDivisionEspecial.class, idApto);
        Recluta recluta = entityManager.find(Recluta.class, idRecluta);
        DivisionEspecial divisionEspecial = entityManager.find(DivisionEspecial.class, idDivisionEspecial);
        if (apto == null || recluta == null || divisionEspecial == null) {
            throw new ReglaRamaException("No encontrado");
        }
        Rama ramaRecluta = recluta.getIdRama();
        Rama ramaDivision = divisionEspecial.getIdDivision().getIdRama();
        if (!Objects.equals(ramaRecluta.getIdRama(), ramaDivision.getIdRama())) {
            throw new ReglaRamaException(
                    "El recluta debe pertenecer a la misma rama que la división de la división especial seleccionada.");
        }
        apto.setIdRecluta(recluta);
        apto.setIdDivisionEspecial(divisionEspecial);
        apto.setAptoServicio(aptoServicio);
        apto.setFechaEvaluacion(fechaEvaluacion);
        apto.setPuntaje(puntaje);
        return entityManager.merge(apto);
    }

    // ==========================================
    // Consultas de soldados adscritos
    // ==========================================

    /**
     * Lista los soldados adscritos a cualquier división especial que pertenezca
     * a la división indicada, sin importar el resultado de la aptitud.
     */
    @Transactional(readOnly = true)
    public List<Recluta> listarReclutasPorDivision(Integer idDivision) {
        String hql = """
                SELECT DISTINCT r
                FROM AptoDivisionEspecial a
                JOIN a.idRecluta r
                JOIN a.idDivisionEspecial de
                JOIN de.idDivision d
                JOIN FETCH r.idRama
                WHERE d.idDivision = :idDivision
                ORDER BY r.apellido, r.nombre
                """;
        return entityManager.createQuery(hql, Recluta.class)
                .setParameter("idDivision", idDivision)
                .getResultList();
    }

    /**
     * Lista únicamente los soldados con aptitud aprobada (aptoServicio = true)
     * adscritos a la división indicada.
     */
    @Transactional(readOnly = true)
    public List<Recluta> listarReclutasAptosPorDivision(Integer idDivision) {
        String hql = """
                SELECT DISTINCT r
                FROM AptoDivisionEspecial a
                JOIN a.idRecluta r
                JOIN a.idDivisionEspecial de
                JOIN de.idDivision d
                JOIN FETCH r.idRama
                WHERE d.idDivision = :idDivision
                  AND a.aptoServicio = :aptoServicio
                ORDER BY r.apellido, r.nombre
                """;
        return entityManager.createQuery(hql, Recluta.class)
                .setParameter("idDivision", idDivision)
                .setParameter("aptoServicio", true)
                .getResultList();
    }

    /**
     * Lista los soldados adscritos a una división especial concreta.
     */
    @Transactional(readOnly = true)
    public List<Recluta> listarReclutasPorDivisionEspecial(Integer idDivisionEspecial) {
        String hql = """
                SELECT DISTINCT r
                FROM AptoDivisionEspecial a
                JOIN a.idRecluta r
                JOIN a.idDivisionEspecial de
                JOIN FETCH r.idRama
                WHERE de.idDivisionEspecial = :idDivisionEspecial
                ORDER BY r.apellido, r.nombre
                """;
        return entityManager.createQuery(hql, Recluta.class)
                .setParameter("idDivisionEspecial", idDivisionEspecial)
                .getResultList();
    }

    /**
     * Lista los registros de adscripción de una división con el recluta, su rama,
     * la división especial y la división ya cargados para su uso en la vista.
     */
    @Transactional(readOnly = true)
    public List<AptoDivisionEspecial> listarAptosPorDivision(Integer idDivision) {
        String hql = """
                SELECT a
                FROM AptoDivisionEspecial a
                JOIN FETCH a.idRecluta r
                JOIN FETCH r.idRama
                JOIN FETCH a.idDivisionEspecial de
                JOIN FETCH de.idDivision d
                WHERE d.idDivision = :idDivision
                ORDER BY r.apellido, r.nombre
                """;
        return entityManager.createQuery(hql, AptoDivisionEspecial.class)
                .setParameter("idDivision", idDivision)
                .getResultList();
    }
}
