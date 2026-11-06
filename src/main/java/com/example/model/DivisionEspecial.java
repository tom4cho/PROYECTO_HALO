package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "divisiones_especiales")
public class DivisionEspecial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDivisionEspecial;

    private String nombre;

    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_division", nullable = false)
    private Division idDivision;

    // ===================
    // Constructor vacío
    // ===================
    public DivisionEspecial() {
    }

    // ===================
    // Constructor sin ID
    // ===================
    public DivisionEspecial(String nombre, String descripcion, Division idDivision) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idDivision = idDivision;
    }

    // ===================
    // Constructor con ID
    // ===================
    public DivisionEspecial(Integer idDivisionEspecial, String nombre, String descripcion, Division idDivision) {
        this.idDivisionEspecial = idDivisionEspecial;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idDivision = idDivision;
    }

    // ===================
    // Getters y setters
    // ===================
    public Integer getIdDivisionEspecial() {
        return idDivisionEspecial;
    }

    public void setIdDivisionEspecial(Integer idDivisionEspecial) {
        this.idDivisionEspecial = idDivisionEspecial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Division getIdDivision() {
        return idDivision;
    }

    public void setIdDivision(Division idDivision) {
        this.idDivision = idDivision;
    }

    @Override
    public String toString() {
        return "DivisionEspecial [idDivisionEspecial=" + idDivisionEspecial + ", nombre=" + nombre
                + ", descripcion=" + descripcion + "]";
    }
}
