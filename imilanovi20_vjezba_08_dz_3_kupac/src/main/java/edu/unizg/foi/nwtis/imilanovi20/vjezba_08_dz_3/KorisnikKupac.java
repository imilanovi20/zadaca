package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;

/**
 * Klasa KorisnikKupac koja je služi za slanje komandi na PosluziteljPartner
 * 
 * @author Ivan Milanović-Litrre
 * @version 1.0.0
 */
public class KorisnikKupac {

  /** Konfiguracija */
  private Konfiguracija konfig;

  /** Putanja datoteka komande. */
  private Path putanjaDatotekaKomande;

  /**
   * Glavna metoda programa
   *
   * @param args niz argumenata koji se proslijeduju u komandnoj liniji prilikom pokretanja
   *        programa.
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Broj argumenata mora biti jednak 2.");
      return;
    }

    var program = new KorisnikKupac();
    var nazivDatotekeKonfig = args[0];

    if (!program.ucitajKonfiguraciju(nazivDatotekeKonfig)) {
      return;
    }
    var nazivDatotekeKomande = args[1];

    if (!program.posaljiKomande(nazivDatotekeKomande)) {
      System.out.println("Dogodila se pogreška");
    }
  }

  /**
   * Metoda koja služi za slanje komandi na odgovarajući poslužitelj na temelju komandi iz datoteke
   *
   * @param nazivDatotekeKomande niz komandi koje se šalju
   * @return vraća true, ako je sve uredu poslano
   */
  private Boolean posaljiKomande(String nazivDatotekeKomande) {
    this.putanjaDatotekaKomande = Path.of(nazivDatotekeKomande);

    try (BufferedReader br =
        Files.newBufferedReader(putanjaDatotekaKomande, StandardCharsets.UTF_8)) {
      String linija;
      while ((linija = br.readLine()) != null) {
        if (linija.trim().isEmpty()) {
          continue;
        }
        String[] stupci = linija.split(";");
        if (stupci.length < 5) {
          System.out.println("Neispravan redak u CSV-u: " + linija);
          continue;
        }
        String korisnik = stupci[0].trim();
        String adresa = stupci[1].trim();
        int mreznaVrata = Integer.parseInt(stupci[2].trim());
        long spavanjeDretve = Long.parseLong(stupci[3].trim());
        String komanda = stupci[4].trim() + "\n";

        try {
          Thread.sleep(spavanjeDretve);
        } catch (InterruptedException e) {
        }

        if (!posaljiKomandu(korisnik, adresa, mreznaVrata, spavanjeDretve, komanda))
          return false;
      }
      return true;
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Metoda koja služi za slanje pojedine komande
   *
   * @param korisnik korisnik koji šalje komandu
   * @param adresa adresa na koju se šalje komanda
   * @param mreznaVrata mrežna vrata na koju se šalje komanda
   * @param spavanjeDretve spavanje dretve između slanja
   * @param komanda komanda koja se šalje
   * @return vraća true ako je sve uredu
   */
  private Boolean posaljiKomandu(String korisnik, String adresa, int mreznaVrata,
      long spavanjeDretve, String komanda) {
    Boolean odgovor = false;
    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda);
      out.flush();
      mreznaUticnica.shutdownOutput();
      var prvaLinija = in.readLine();
      var odgovorKomande = new StringBuilder();
      String linija;
      while ((linija = in.readLine()) != null) {
        odgovorKomande.append(linija).append("\n");
      }
      mreznaUticnica.shutdownInput();;
      if (prvaLinija != null)
        odgovor = true;
      mreznaUticnica.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return odgovor;

  }

  /**
   * Metoda koja služi za učitavanje konfiguracijske datoteke
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @return vraća true, ako je uspješno učitavanje konfiguracije
   */
  private boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }

}
