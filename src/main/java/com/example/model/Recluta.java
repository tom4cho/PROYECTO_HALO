package com.example.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reclutas")
public class Recluta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecluta;

    private String nombre;

    private String apellido;

    private Integer altura;

    @Column(precision = 6, scale = 3)
    private BigDecimal peso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rama", nullable = false)
    private Rama idRama;

    @OneToOne(mappedBy = "idRecluta", optional = true)
    private AptoDivisionEspecial idApto;

    // ===================
    // Constructor vacío
    // ===================
    public Recluta() {
    }

    // ===================
    // Constructor sin ID
    // ===================
    public Recluta(String nombre, String apellido, Integer altura, BigDecimal peso, Rama idRama,
            AptoDivisionEspecial idApto) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.altura = altura;
        this.peso = peso;
        this.idRama = idRama;
        this.idApto = idApto;
    }

    // ===================
    // Constructor con ID
    // ===================
    public Recluta(Integer idRecluta, String nombre, String apellido, Integer altura, BigDecimal peso, Rama idRama,
            AptoDivisionEspecial idApto) {
        this.idRecluta = idRecluta;
        this.nombre = nombre;
        this.apellido = apellido;
        this.altura = altura;
        this.peso = peso;
        this.idRama = idRama;
        this.idApto = idApto;
    }

    // ===================
    // Getters y setters
    // ===================
    public Integer getIdRecluta() {
        return idRecluta;
    }

    public void setIdRecluta(Integer idRecluta) {
        this.idRecluta = idRecluta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public Rama getIdRama() {
        return idRama;
    }

    public void setIdRama(Rama idRama) {
        this.idRama = idRama;
    }

    public AptoDivisionEspecial getIdApto() {
        return idApto;
    }

    public void setIdApto(AptoDivisionEspecial idApto) {
        this.idApto = idApto;
    }

    @Override
    public String toString() {
        return "Recluta [idRecluta=" + idRecluta + ", nombre=" + nombre + ", apellido=" + apellido + ", altura="
                + altura + ", peso=" + peso + "]";
    }
}
