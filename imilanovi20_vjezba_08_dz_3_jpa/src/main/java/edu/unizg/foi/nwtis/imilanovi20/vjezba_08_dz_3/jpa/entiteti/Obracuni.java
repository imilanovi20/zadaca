package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.jpa.entiteti;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;


/**
 * The persistent class for the OBRACUNI database table.
 * 
 */
@Entity
@NamedQuery(name = "Obracuni.findAll", query = "SELECT o FROM Obracuni o")
public class Obracuni implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @SequenceGenerator(name = "OBRACUNI_RB_GENERATOR", sequenceName = "OBRACUNI_ID", initialValue = 1,
      allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OBRACUNI_RB_GENERATOR")
  private int rb;

  private double cijena;

  private String id;

  private boolean jelo;

  private double kolicina;

  private Timestamp vrijeme;

  // bi-directional many-to-one association to Partneri
  @ManyToOne
  @JoinColumn(name = "PARTNER")
  private Partneri partneri;

  public Obracuni() {}

  public int getRb() {
    return this.rb;
  }

  public void setRb(int rb) {
    this.rb = rb;
  }

  public double getCijena() {
    return this.cijena;
  }

  public void setCijena(double cijena) {
    this.cijena = cijena;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean getJelo() {
    return this.jelo;
  }

  public void setJelo(boolean jelo) {
    this.jelo = jelo;
  }

  public double getKolicina() {
    return this.kolicina;
  }

  public void setKolicina(double kolicina) {
    this.kolicina = kolicina;
  }

  public Timestamp getVrijeme() {
    return this.vrijeme;
  }

  public void setVrijeme(Timestamp vrijeme) {
    this.vrijeme = vrijeme;
  }

  public Partneri getPartneri() {
    return this.partneri;
  }

  public void setPartneri(Partneri partneri) {
    this.partneri = partneri;
  }

}
