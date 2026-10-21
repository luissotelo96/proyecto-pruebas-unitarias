package org.example.lsc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "traducciones")
public class Traduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String codigoSena;

    @Column(nullable = false, length = 80)
    private String texto;

    @Column(nullable = false, length = 10)
    private String modo;

    @Column(nullable = false)
    private Double confianza;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    public Traduccion() {
    }

    public Traduccion(String codigoSena, String texto, String modo, Double confianza) {
        this.codigoSena = codigoSena;
        this.texto = texto;
        this.modo = modo;
        this.confianza = confianza;
    }

    @PrePersist
    void asignarFecha() {
        if (creadoEn == null) {
            creadoEn = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigoSena() {
        return codigoSena;
    }

    public String getTexto() {
        return texto;
    }

    public String getModo() {
        return modo;
    }

    public Double getConfianza() {
        return confianza;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
