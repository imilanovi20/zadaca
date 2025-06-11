package org.unizg.foi.nwtis.imilanovi20.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Konfiguracija RESTful poslužitelja
 * 
 * @author Ivan Milanović-Litre
 * @version 1.1.0
 */
@ApplicationPath("nwtis/v1")
public class RestConfiguration extends Application {

  /** Korisnicko ime baza podataka. */
  @Inject
  @ConfigProperty(name = "korisnickoImeBazaPodataka")
  private String korisnickoImeBazaPodataka;

  /** Lozinka baza podataka. */
  @Inject
  @ConfigProperty(name = "lozinkaBazaPodataka")
  private String lozinkaBazaPodataka;

  /** Upravljac baza podataka. */
  @Inject
  @ConfigProperty(name = "upravljacBazaPodataka")
  private String upravljacBazaPodataka;

  /** Url baza podataka. */
  @Inject
  @ConfigProperty(name = "urlBazaPodataka")
  private String urlBazaPodataka;

  /**
   * Daj vezu.
   *
   * @return veza baze podataka
   * @throws Exception greska
   */
  public Connection dajVezu() throws Exception {
    Class.forName(this.upravljacBazaPodataka);
    var vezaBazaPodataka = DriverManager.getConnection(this.urlBazaPodataka,
        this.korisnickoImeBazaPodataka, this.lozinkaBazaPodataka);
    return vezaBazaPodataka;
  }
}
