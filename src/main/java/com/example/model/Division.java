package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "divisiones")
public class Division {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDivision;

    private String nombre;

    private String descripcion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rama", nullable = false)
    private Rama idRama;

    // ===================
    // Constructor vacío
    // ===================
    public Division() {
    }

    // ===================
    // Constructor sin ID
    // ===================
    public Division(String nombre, String descripcion, Rama idRama) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idRama = idRama;
    }

    // ===================
    // Constructor con ID
    // ===================
    public Division(Integer idDivision, String nombre, String descripcion, Rama idRama) {
        this.idDivision = idDivision;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idRama = idRama;
    }

    // ===================
    // Getters y setters
    // ===================
    public Integer getIdDivision() {
        return idDivision;
    }

    public void setIdDivision(Integer idDivision) {
        this.idDivision = idDivision;
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

    public Rama getIdRama() {
        return idRama;
    }

    public void setIdRama(Rama idRama) {
        this.idRama = idRama;
    }

    @Override
    public String toString() {
        return "Division [idDivision=" + idDivision + ", nombre=" + nombre + ", descripcion=" + descripcion + "]";
    }
}
