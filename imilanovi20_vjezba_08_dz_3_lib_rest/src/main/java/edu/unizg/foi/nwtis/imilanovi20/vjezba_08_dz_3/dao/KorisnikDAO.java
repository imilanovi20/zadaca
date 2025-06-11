package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.podaci.Korisnik;


/**
 * Klasa KorisnikDAO omogućava pristup i upravljanje podacima o korisnicima u bazi podataka.
 * 
 * @author Ivan Milanović-Litre
 * @version 1.1.0
 */
public class KorisnikDAO {

  /** Veza baze podataka */
  private Connection vezaBP;

  /**
   * Konstruktor KorisnikDAO
   *
   * @param vezaBP veza baze podataka
   */
  public KorisnikDAO(Connection vezaBP) {
    super();
    this.vezaBP = vezaBP;
  }

  /**
   * Dohvati.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka
   * @param prijava prijava
   * @return korisnik
   */
  public Korisnik dohvati(String korisnik, String lozinka, Boolean prijava) {
    String upit = "SELECT ime, prezime, korisnik, lozinka, email FROM korisnici WHERE korisnik = ?";

    if (prijava) {
      upit += " and lozinka = ?";
    }

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, korisnik);
      if (prijava) {
        s.setString(2, lozinka);
      }
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");

        Korisnik k = new Korisnik(korisnik, "******", prezime, ime, email);
        return k;
      }

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvati sve.
   *
   * @return lista korisnika
   */
  public List<Korisnik> dohvatiSve() {
    String upit = "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici";

    List<Korisnik> korisnici = new ArrayList<>();

    try (Statement s = this.vezaBP.createStatement(); ResultSet rs = s.executeQuery(upit)) {

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Dohvati prezime i ime.
   *
   * @param pPrezime prezime
   * @param pIme ime
   * @return lista korisnika
   */
  public List<Korisnik> dohvatiPrezimeIme(String pPrezime, String pIme) {
    String upit =
        "SELECT ime, prezime, email, korisnik, lozinka FROM korisnici WHERE prezime LIKE ? AND ime LIKE ?";

    List<Korisnik> korisnici = new ArrayList<>();

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit);) {

      s.setString(1, pPrezime);
      s.setString(2, pIme);
      ResultSet rs = s.executeQuery();

      while (rs.next()) {
        String korisnik1 = rs.getString("korisnik");
        String ime = rs.getString("ime");
        String prezime = rs.getString("prezime");
        String email = rs.getString("email");
        Korisnik k = new Korisnik(korisnik1, "******", prezime, ime, email);

        korisnici.add(k);
      }
      rs.close();
      return korisnici;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  /**
   * Azuriraj.
   *
   * @param k korisnik
   * @param lozinka lozinka
   * @return true ako je korisnik uspiješno azuriran
   */
  public boolean azuriraj(Korisnik k, String lozinka) {
    String upit = "UPDATE korisnici SET ime = ?, prezime = ?, email = ?, lozinka = ? "
        + " WHERE korisnik = ?";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, lozinka);
      s.setString(5, k.korisnik());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (SQLException ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

  /**
   * Dodaj.
   *
   * @param k korisnik
   * @return true ako je uspiješno spremljen
   */
  public boolean dodaj(Korisnik k) {
    String upit = "INSERT INTO korisnici (ime, prezime, email, korisnik, lozinka) "
        + "VALUES (?, ?, ?, ?, ?)";

    try (PreparedStatement s = this.vezaBP.prepareStatement(upit)) {

      s.setString(1, k.ime());
      s.setString(2, k.prezime());
      s.setString(3, k.email());
      s.setString(4, k.korisnik());
      s.setString(5, k.lozinka());

      int brojAzuriranja = s.executeUpdate();

      return brojAzuriranja == 1;

    } catch (Exception ex) {
      Logger.getLogger(KorisnikDAO.class.getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
