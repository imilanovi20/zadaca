package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Obracun;

/**
 * Klasa ObracunDAO omogućava pristup i upravljanje podacima o obračunima u bazi podataka.
 * 
 * @author Ivan Milanović-Litre
 * @version 1.1.0
 */
public class ObracunDAO {

  /** Veza baze podataka */
  private Connection vezaBP;

  /**
   * Konsktruktor ObracunDAO
   *
   * @param vezaBP the veza BP
   */
  public ObracunDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }


  /**
   * Dohvati sve.
   *
   * @return lista obračuna
   */
  public List<Obracun> dohvatiSve() {
    String upit = "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni ORDER BY id";
    List<Obracun> obracuni = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        int partner = rs.getInt("partner");
        String id = rs.getString("id");
        Boolean jelo = rs.getBoolean("jelo");
        float kolicina = rs.getFloat("kolicina");
        float cijena = rs.getFloat("cijena");
        Long vrijeme = rs.getTimestamp("vrijeme").getTime();

        Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
        obracuni.add(o);
      }
      return obracuni;

    } catch (SQLException ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  /**
   * Dohvati obračune unutar intervala.
   *
   * @param odVr početno vrijeme
   * @param doVr završno vrijeme
   * @return lista obračuna
   */
  public List<Obracun> dohvatiInterval(Long odVr, Long doVr) {
    StringBuilder upit = new StringBuilder();
    upit.append("SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE 1=1");
    List<Object> parametri = new ArrayList<>();
    if (odVr != null) {
      upit.append(" AND vrijeme >= ?");
      parametri.add(new Timestamp(odVr));
    }
    if (doVr != null) {
      upit.append(" AND vrijeme <= ?");
      parametri.add(new Timestamp(doVr));
    }
    upit.append(" ORDER BY id");
    String upitSlanje = upit.toString();
    List<Obracun> obracuni = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upitSlanje)) {
      for (int i = 0; i < parametri.size(); i++) {
        s.setTimestamp(i + 1, (Timestamp) parametri.get(i));
      }

      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          Boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          Long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(o);
        }
        return obracuni;
      }

    } catch (SQLException ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvati obračune jela unutar intervala.
   *
   * @param odVr početno vrijeme
   * @param doVr završno vrijeme
   * @return lista obračuna
   */
  public List<Obracun> dohvatiJeloInterval(Long odVr, Long doVr) {
    StringBuilder upitDijelovi = new StringBuilder();
    upitDijelovi.append(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = true");
    List<Object> parametri = new ArrayList<>();
    if (odVr != null) {
      upitDijelovi.append(" AND vrijeme >= ?");
      parametri.add(new Timestamp(odVr));
    }
    if (doVr != null) {
      upitDijelovi.append(" AND vrijeme <= ?");
      parametri.add(new Timestamp(doVr));
    }
    upitDijelovi.append(" ORDER BY id");
    String upit = upitDijelovi.toString();
    List<Obracun> obracuni = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      for (int i = 0; i < parametri.size(); i++) {
        s.setTimestamp(i + 1, (Timestamp) parametri.get(i));
      }
      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          Boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          Long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(o);
        }
        return obracuni;
      }

    } catch (SQLException ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;

  }

  /**
   * Dohvati obračune pića unutar intervala.
   *
   * @param odVr početno vrijeme
   * @param doVr završno vrijeme
   * @return lista obračuna
   */
  public List<Obracun> dohvatiPiceInterval(Long odVr, Long doVr) {
    StringBuilder upitDijelovi = new StringBuilder();
    upitDijelovi.append(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE jelo = false");
    List<Object> parametri = new ArrayList<>();
    if (odVr != null) {
      upitDijelovi.append(" AND vrijeme >= ?");
      parametri.add(new Timestamp(odVr));
    }
    if (doVr != null) {
      upitDijelovi.append(" AND vrijeme <= ?");
      parametri.add(new Timestamp(doVr));
    }
    upitDijelovi.append(" ORDER BY id");
    String upit = upitDijelovi.toString();
    List<Obracun> obracuni = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      for (int i = 0; i < parametri.size(); i++) {
        s.setTimestamp(i + 1, (Timestamp) parametri.get(i));
      }

      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          Boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          Long vrijeme = rs.getTimestamp("vrijeme").getTime();

          Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(o);
        }
        return obracuni;
      }

    } catch (SQLException ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvati obračune partnera unutar intervala.
   *
   * @param partnerId id partnera
   * @param odVr početno vrijeme
   * @param doVr završno vrijeme
   * @return lista obračuna
   */
  public List<Obracun> dohvatiPartnerInterval(int partnerId, Long odVr, Long doVr) {
    StringBuilder upitDijelovi = new StringBuilder();
    upitDijelovi.append(
        "SELECT partner, id, jelo, kolicina, cijena, vrijeme FROM obracuni WHERE partner = ?");
    List<Object> parametri = new ArrayList<>();
    parametri.add(partnerId);
    if (odVr != null) {
      upitDijelovi.append(" AND vrijeme >= ?");
      parametri.add(new Timestamp(odVr));
    }
    if (doVr != null) {
      upitDijelovi.append(" AND vrijeme <= ?");
      parametri.add(new Timestamp(doVr));
    }
    upitDijelovi.append(" ORDER BY id");
    String upit = upitDijelovi.toString();
    List<Obracun> obracuni = new ArrayList<>();
    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {
      s.setInt(1, partnerId);
      for (int i = 1; i < parametri.size(); i++) {
        s.setTimestamp(i + 1, (Timestamp) parametri.get(i));
      }
      try (ResultSet rs = s.executeQuery()) {
        while (rs.next()) {
          int partner = rs.getInt("partner");
          String id = rs.getString("id");
          Boolean jelo = rs.getBoolean("jelo");
          float kolicina = rs.getFloat("kolicina");
          float cijena = rs.getFloat("cijena");
          Long vrijeme = rs.getTimestamp("vrijeme").getTime();
          Obracun o = new Obracun(partner, id, jelo, kolicina, cijena, vrijeme);
          obracuni.add(o);
        }
        return obracuni;
      }
    } catch (SQLException ex) {
      Logger.getLogger(ObracunDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dodaj obračune.
   *
   * @param obracuni obracuni
   * @return true ako su svi obračuni uspiješno dodani
   */
  public boolean dodaj(Obracun[] obracuni) {
    for (Obracun o : obracuni) {
      if (!dodajJedan(o)) {
        return false;
      }
    }
    return true;
  }


  /**
   * Dodaj jedan obračun.
   *
   * @param o obračun
   * @return true ako je uspiješno spremljen
   */
  private boolean dodajJedan(Obracun o) {
    String upit = "INSERT INTO obracuni (partner, id, jelo, kolicina, cijena, vrijeme) "
        + "VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setInt(1, o.partner());
      s.setString(2, o.id());
      s.setBoolean(3, o.jelo());
      s.setFloat(4, o.kolicina());
      s.setFloat(5, o.cijena());
      s.setTimestamp(6, new Timestamp(o.vrijeme()));

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(PartnerDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
