package com.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ramas")
public class Rama {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRama;

    private String nombre;

    private String descripcion;

    // ===================
    // Constructor vacío
    // ===================
    public Rama() {
    }

    // ===================
    // Constructor sin ID
    // ===================
    public Rama(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // ===================
    // Constructor con ID
    // ===================
    public Rama(Integer idRama, String nombre, String descripcion) {
        this.idRama = idRama;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // ===================
    // Getters y setters
    // ===================
    public Integer getIdRama() {
        return idRama;
    }

    public void setIdRama(Integer idRama) {
        this.idRama = idRama;
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

    @Override
    public String toString() {
        return "Rama [idRama=" + idRama + ", nombre=" + nombre + ", descripcion=" + descripcion + "]";
    }
}
