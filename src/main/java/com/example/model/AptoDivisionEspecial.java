package com.example.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "aptos_division_especial")
public class AptoDivisionEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idApto;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_recluta", nullable = false, unique = true)
    private Recluta idRecluta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_division_especial", nullable = false)
    private DivisionEspecial idDivisionEspecial;

    private Boolean aptoServicio;

    private LocalDate fechaEvaluacion;

    private Integer puntaje;

    // ===================
    // Constructor vacío
    // ===================
    public AptoDivisionEspecial() {
    }

    // ===================
    // Constructor sin ID
    // ===================
    public AptoDivisionEspecial(Recluta idRecluta, DivisionEspecial idDivisionEspecial, Boolean aptoServicio,
            LocalDate fechaEvaluacion, Integer puntaje) {
        this.idRecluta = idRecluta;
        this.idDivisionEspecial = idDivisionEspecial;
        this.aptoServicio = aptoServicio;
        this.fechaEvaluacion = fechaEvaluacion;
        this.puntaje = puntaje;
    }

    // ===================
    // Constructor con ID
    // ===================
    public AptoDivisionEspecial(Integer idApto, Recluta idRecluta, DivisionEspecial idDivisionEspecial,
            Boolean aptoServicio, LocalDate fechaEvaluacion, Integer puntaje) {
        this.idApto = idApto;
        this.idRecluta = idRecluta;
        this.idDivisionEspecial = idDivisionEspecial;
        this.aptoServicio = aptoServicio;
        this.fechaEvaluacion = fechaEvaluacion;
        this.puntaje = puntaje;
    }

    // ===================
    // Getters y setters
    // ===================
    public Integer getIdApto() {
        return idApto;
    }

    public void setIdApto(Integer idApto) {
        this.idApto = idApto;
    }

    public Recluta getIdRecluta() {
        return idRecluta;
    }

    public void setIdRecluta(Recluta idRecluta) {
        this.idRecluta = idRecluta;
    }

    public DivisionEspecial getIdDivisionEspecial() {
        return idDivisionEspecial;
    }

    public void setIdDivisionEspecial(DivisionEspecial idDivisionEspecial) {
        this.idDivisionEspecial = idDivisionEspecial;
    }

    public Boolean getAptoServicio() {
        return aptoServicio;
    }

    public void setAptoServicio(Boolean aptoServicio) {
        this.aptoServicio = aptoServicio;
    }

    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(LocalDate fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public Integer getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Integer puntaje) {
        this.puntaje = puntaje;
    }

    @Override
    public String toString() {
        return "AptoDivisionEspecial [idApto=" + idApto + ", aptoServicio=" + aptoServicio + ", fechaEvaluacion="
                + fechaEvaluacion + ", puntaje=" + puntaje + "]";
    }
}
