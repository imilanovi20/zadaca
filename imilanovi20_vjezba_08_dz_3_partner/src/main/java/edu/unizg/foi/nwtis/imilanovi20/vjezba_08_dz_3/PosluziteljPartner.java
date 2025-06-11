package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import edu.unizg.foi.nwtis.podaci.Obracun;

// TODO: Auto-generated Javadoc
/**
 * Klasa PoslužiteljPartner koja je služi za registraciju partnera, slanje zahtjeva za kraj te
 * pokretanje dretve na kojoj se nalazi poslužitelj za prijem zahtjeva od kupca.
 *
 * @author Ivan Milanović-Litre
 * @version 1.2.0
 */
public class PosluziteljPartner {

  /** Konfiguracijski podaci. */
  private Konfiguracija konfig;

  /** Jelovnik. */
  private Map<String, Jelovnik> jelovnik = new ConcurrentHashMap<>();

  /** Karta pića. */
  private Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Izvršitelj. */
  private ExecutorService izvrsitelj = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kvota narudžbi. */
  private int kvotaNarudzbi = 5;

  /** Broj naplaćenih narudžbi. */
  private int brojNaplacenihNarudzbi = 0;

  /** Kraj. */
  private AtomicBoolean kraj = new AtomicBoolean(false);

  /** Prekinute veze na dretvi za prijem zahteva. */
  private AtomicInteger prekinutoPrijem = new AtomicInteger(0);

  /** The prekinuto kraj. */
  private AtomicInteger prekinutoKraj = new AtomicInteger(0);

  /** Zaključavanje. */
  private static Lock zakljucavanje = new ReentrantLock();

  /** Aktivne dretve. */
  private Queue<Future<?>> aktivneDretve = new ConcurrentLinkedQueue<>();

  /** Otvorene narudzbe. */
  private Map<String, List<Narudzba>> otvoreneNarudzbe = new ConcurrentHashMap<>();

  /** Plaćene narudžbe. */
  private Map<String, List<Narudzba>> placeneNarudzbe = new ConcurrentHashMap<>();

  /** Pauza prijem. */
  private AtomicBoolean pauzaPrijem = new AtomicBoolean(false);

  /** Kod za admin partnera. */
  private String kodZaAdminPartnera = "";

  /** Kod kraj. */
  private String kodKraj = "";

  /** Predložak kraj. */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ$");

  /** Predložak partner. */
  private Pattern predlozakPartner = Pattern.compile("^PARTNER$");

  /** Predložak jelovnik. */
  private Pattern predlozakJelovnik = Pattern.compile("^JELOVNIK\\s+(?<korisnik>\\S+)$");

  /** Predložak karta pica. */
  private Pattern predlozakKartapica = Pattern.compile("^KARTAPIĆA\\s+(?<korisnik>\\S+)$");

  /** Predložak narudzba. */
  private Pattern predlozakNarudzba = Pattern.compile("^NARUDŽBA\\s+(?<korisnik>\\S+)$");

  /** Predložak jelo. */
  private Pattern predlozakJelo = Pattern
      .compile("^JELO\\s+(?<korisnik>\\S+)\\s+(?<idJela>\\S+)\\s+(?<kolicina>\\d+(\\.\\d+)?)$");

  /** Predložak pice. */
  private Pattern predlozakPice = Pattern
      .compile("^PIĆE\\s+(?<korisnik>\\S+)\\s+(?<idPica>\\S+)\\s+(?<kolicina>\\d+(\\.\\d+)?)$");

  /** Predložak racun. */
  private Pattern predlozakRacun = Pattern.compile("^RAČUN\\s+(?<korisnik>\\S+)$");

  /** Predlozak kraj kod. */
  private Pattern predlozakKrajKod = Pattern.compile("^KRAJ\\s+(?<kod>\\w+)$");

  /** Predlozak osvjezi. */
  private Pattern predlozakOsvjezi = Pattern.compile("^OSVJEŽI\\s+(?<kod>\\w+)$");

  /** Predlozak status. */
  private Pattern predlozakStatus = Pattern.compile("^STATUS\\s+(?<kod>\\w+)\\s+(?<dio>[1])$");

  /** Predlozak pauza. */
  private Pattern predlozakPauza = Pattern.compile("^PAUZA\\s+(?<kod>\\w+)\\s+(?<dio>[1])$");

  /** Predlozak start. */
  private Pattern predlozakStart = Pattern.compile("^START\\s+(?<kod>\\w+)\\s+(?<dio>[1])$");

  /** Predlozak spava. */
  private Pattern predlozakSpava = Pattern.compile("^SPAVA\\s+(?<kod>\\w+)\\s+(?<spavanje>\\d+)$");

  /** Predlozak stanje. */
  private Pattern predlozakStanje = Pattern.compile("^STANJE\\s+(?<korisnik>\\S+)$");



  /**
   * Glavna metoda programa.
   *
   * @param args niz argumenata koji se proslijeduju u komandnoj liniji prilikom pokretanja
   *        programa.
   */
  public static void main(String[] args) {
    if (args.length > 2) {
      System.out.println("Broj argumenata veći od 2.");
      return;
    }
    var program = new PosluziteljPartner();
    program.registrirajPrislinoGasenje();
    var nazivDatoteke = args[0];
    if (!program.ucitajKonfiguraciju(nazivDatoteke)) {
      return;
    }
    if (args.length == 1) {
      program.registrirajPartnera();
      return;
    }
    var linija = args[1];
    var poklapanjeKraj = program.predlozakKraj.matcher(linija);
    var poklapanjePartner = program.predlozakPartner.matcher(linija);
    if (poklapanjeKraj.matches()) {
      program.posaljiKraj();
      return;
    } else if (poklapanjePartner.matches()) {
      program.pripremiKreni();
      return;
    }
  }

  /**
   * Metoda koja služi za registraciju aktivnosti prilikom prisilnog gašenja programa.
   */
  private void registrirajPrislinoGasenje() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("\nKraj...");
      kraj.set(true);
      int brojPrekinutihDretvi = 0;
      for (var dretva : aktivneDretve) {
        if (!dretva.isDone()) {
          dretva.cancel(true);
          brojPrekinutihDretvi++;
        }
      }
      System.out
          .println("Prekinuto (dretva za prijem zahtjeva kupaca): " + this.prekinutoPrijem.get());
      System.out.println("Prekinuto (dretva za kraj): " + this.prekinutoKraj.get());
      System.out.println("Broj prekinutih dretvi: " + brojPrekinutihDretvi);
    }));
  }


  /**
   * Metoda koja služi za spajanje na poslužitelj tvrtka te registraciju partnera.
   */
  private void registrirajPartnera() {
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));

    var komanda = kreirajKomanduRegistracijaPartnera();

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda);
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();

      if (linija != null && linija.startsWith("OK ")) {
        String[] odgovorDijelovi = linija.trim().split("\\s+");
        if (odgovorDijelovi.length == 2) {
          String sigKod = odgovorDijelovi[1];
          String postojeciSigKod = this.konfig.dajPostavku("sigKod");
          if (postojeciSigKod != null) {
            this.konfig.azurirajPostavku("sigKod", sigKod);
          } else {
            this.konfig.spremiPostavku("sigKod", sigKod);
          }
          try {
            this.konfig.spremiKonfiguraciju();
          } catch (NeispravnaKonfiguracija ex) {
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Metoda koja služi za spajanje na poslužitelj tvrtka te slanje zahtjeva za kraj.
   */
  private void posaljiKraj() {
    var komanda = kreirajKomanduKraj();
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrata);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda);
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      if (linija.equals("OK")) {
        this.kraj.set(true);
      }
      mreznaUticnica.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Metoda koja služi za kreiranje potrebne dretve te pokretanje potrebnog poslužitelja.
   */
  private void pripremiKreni() {
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));
    this.kvotaNarudzbi = Integer.parseInt(this.konfig.dajPostavku("kvotaNarudzbi"));
    this.kodZaAdminPartnera = this.konfig.dajPostavku("kodZaAdmin");
    this.kodKraj = this.konfig.dajPostavku("kodZaKraj");

    sacekajTvrtku(adresa, mreznaVrataRad);

    if (!ucitajJelovnikIKartuPica(adresa, mreznaVrataRad))
      return;

    var graditelj = Thread.ofVirtual();
    var tvornicaDretvi = graditelj.factory();
    this.izvrsitelj = Executors.newThreadPerTaskExecutor(tvornicaDretvi);

    var dretvaPrijem = this.izvrsitelj.submit(() -> this.pokreniPosluziteljaPrijem());
    aktivneDretve.add(dretvaPrijem);
    var dretvaKraj = this.izvrsitelj.submit(() -> this.pokreniPosluziteljaKraj());
    aktivneDretve.add(dretvaKraj);

    while (!dretvaPrijem.isDone() && !dretvaKraj.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Sacekaj tvrtku.
   *
   * @param adresa adresa
   * @param mreznaVrataRad mrezna vrata rad
   */
  private void sacekajTvrtku(String adresa, int mreznaVrataRad) {
    while (true) {
      try (Socket mreznaUticnica = new Socket(adresa, mreznaVrataRad)) {
        break;
      } catch (Exception e) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }

  /**
   * Metoda koja služi za pokretanje posluzitelja kraj.
   */
  private void pokreniPosluziteljaKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKrajPartner"));
    var brojCekaca = 0;
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          this.obradiKraj(mreznaUticnica);
        } catch (Exception e) {
          if (!this.kraj.get())
            this.prekinutoKraj.incrementAndGet();
        }
      }
      ss.close();

    } catch (IOException e) {
    }
  }

  /**
   * Metoda koja služi za pokretanje poslužitelja za prijem zahtjeva od kupca.
   */
  private void pokreniPosluziteljaPrijem() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrata"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          var dretva = this.izvrsitelj.submit(() -> obradiPrijem(mreznaUticnica));
          aktivneDretve.add(dretva);
        } catch (Exception e) {
          if (!this.kraj.get())
            this.prekinutoPrijem.incrementAndGet();
        }
      }
      ss.close();
    } catch (IOException e) {
    }
  }

  /**
   * Metoda koja obrađuje zahtjev koji je poslan na poslužitelj za kraj, za početak provjerava
   * zahtjev koji je poslan te na temelju njega poziva pripadajuću metodu te kreira odgovor na
   * zahtjev.
   *
   * @param mreznaUticnica mrezna utičnica poslužitelja za kraj
   */
  private void obradiKraj(Socket mreznaUticnica) {
    PrintWriter out = null;
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      String linija = in.readLine();

      Matcher poklapanjeKrajKod = predlozakKrajKod.matcher(linija);
      Matcher poklapanjeOsvjezi = predlozakOsvjezi.matcher(linija);
      Matcher poklapanjeStatus = predlozakStatus.matcher(linija);
      Matcher poklapanjePauza = predlozakPauza.matcher(linija);
      Matcher poklapanjeStart = predlozakStart.matcher(linija);
      Matcher poklapanjeSpava = predlozakSpava.matcher(linija);

      String odgovor = null;

      if (poklapanjeKrajKod.matches()) {
        odgovor = obradiKomanduKrajKod(poklapanjeKrajKod);
      } else if (poklapanjeOsvjezi.matches()) {
        odgovor = obradiKomanduOsvjezi(poklapanjeOsvjezi);
      } else if (poklapanjeStatus.matches()) {
        odgovor = obradiKomanduStatus(poklapanjeStatus);
      } else if (poklapanjePauza.matches()) {
        odgovor = obradiKomanduPauza(poklapanjePauza);
      } else if (poklapanjeStart.matches()) {
        odgovor = obradiKomanduStart(poklapanjeStart);
      } else if (poklapanjeSpava.matches()) {
        odgovor = obradiKomanduSpava(poklapanjeSpava);
      } else {
        odgovor = "ERROR 60 - Format komande nije ispravan";
      }

      out.println(odgovor);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {
      if (out != null) {
        out.write("ERROR 69 - Neuspješna obrada komande KRAJ\n");
        out.flush();
      }
    }
  }

  /**
   * Metoda koja obrađuje spavanje dretve te generira pripadajući odgovor.
   *
   * @param poklapanjeSpava objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduSpava(Matcher poklapanjeSpava) {
    try {
      int spavanje = Integer.parseInt(poklapanjeSpava.group("spavanje"));
      try {
        Thread.sleep(spavanje);
        return "OK\n";
      } catch (InterruptedException e) {
        return "ERROR 63 – Prekid spavanja dretve\n";
      }
    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande SPAVA\n";
    }
  }


  /**
   * Metoda koja postavlja start na određeni dio poslužitelja te generira pripadajući odgvor.
   *
   * @param poklapanjeStart objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduStart(Matcher poklapanjeStart) {
    try {
      String kod = poklapanjeStart.group("kod");

      if (!kod.equals(this.kodZaAdminPartnera)) {
        return "ERROR 61 – Pogrešan kodZaAdminPartnera\n";
      }
      if (this.pauzaPrijem.get()) {
        this.pauzaPrijem.set(false);
        return "OK\n";
      } else {
        return "ERROR 62 – Pogrešna promjena pauze ili starta\n";
      }

    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande PAUZA\n";
    }
  }

  /**
   * Metoda koja postavlja pauzu na određeni dio poslužitelja te generira pripadajući odgovor.
   *
   * @param poklapanjePauza objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduPauza(Matcher poklapanjePauza) {
    try {
      String kod = poklapanjePauza.group("kod");

      if (!kod.equals(this.kodZaAdminPartnera)) {
        return "ERROR 61 - Pogrešan kodZaAdminPartnera\n";
      }

      if (!this.pauzaPrijem.get()) {
        this.pauzaPrijem.set(true);
        return "OK\n";
      } else {
        return "ERROR 62 - Pogrešna promjena pauze ili starta\n";
      }

    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande PAUZA\n";
    }
  }

  /**
   * Metoda koja služi za dohvaćanje stanja određenog dijela poslužitelja te generira pripadajući
   * odgovor.
   *
   * @param poklapanjeStatus objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK[0,1] - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduStatus(Matcher poklapanjeStatus) {
    try {
      String kod = poklapanjeStatus.group("kod");

      if (!kod.equals(this.kodZaAdminPartnera)) {
        return "ERROR 61 - Pogrešan kodZaAdminPartnera\n";
      }

      return "OK " + (this.pauzaPrijem.get() ? "0" : "1") + "\n";

    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande STATUS\n";
    }
  }

  /**
   * Metoda koja služi za osvježavanje podataka te generira pripadajući odgovor.
   *
   * @param poklapanjeOsvjezi objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduOsvjezi(Matcher poklapanjeOsvjezi) {
    try {
      String kod = poklapanjeOsvjezi.group("kod");

      if (!kod.equals(this.kodZaAdminPartnera)) {
        return "ERROR 61 - Pogrešan kodZaAdminPartnera\n";
      }

      var adresa = this.konfig.dajPostavku("adresa");
      var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
      if (ucitajJelovnikIKartuPica(adresa, mreznaVrataRad)) {
        return "OK\n";
      } else {
        return "ERROR 69 - Problem kod osvježavanja podataka\n";
      }

    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande OSVJEŽI\n";
    }
  }

  /**
   * Metoda koja služi slanje koda za kraj te generira pripadajući odgovor.
   *
   * @param poklapanjeKrajKod objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduKrajKod(Matcher poklapanjeKrajKod) {
    try {
      String kod = poklapanjeKrajKod.group("kod");

      if (kod.equals(this.kodKraj)) {
        this.kraj.set(true);
        return "OK\n";
      } else {
        return "ERROR 60 - Neispravan kod za kraj\n";
      }
    } catch (Exception e) {
      return "ERROR 69 - Neuspješna obrada komande KRAJ\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev koji je poslan na poslužitelj za prijem zahtjeva od kupca, za
   * početak provjerava zahtjev koji je poslan te na temelju njega poziva pripadajuću metodu te
   * kreira odgovor na zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica poslužitelja za prijem
   */
  private void obradiPrijem(Socket mreznaUticnica) {
    PrintWriter out = null;
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();

      Matcher poklapanjeJelovnik = predlozakJelovnik.matcher(linija);
      Matcher poklapanjeKartaPica = predlozakKartapica.matcher(linija);
      Matcher poklapanjeNarudzba = predlozakNarudzba.matcher(linija);
      Matcher poklapanjeJelo = predlozakJelo.matcher(linija);
      Matcher poklapanjePice = predlozakPice.matcher(linija);
      Matcher poklapanjeRacun = predlozakRacun.matcher(linija);
      Matcher poklapanjeStanje = predlozakStanje.matcher(linija);

      String odgovor = null;
      if (!this.pauzaPrijem.get()) {
        if (poklapanjeJelovnik.matches()) {
          odgovor = obradiKomanduJelovnik();
        } else if (poklapanjeKartaPica.matches()) {
          odgovor = obradiKomanduKartaPica();
        } else if (poklapanjeNarudzba.matches()) {
          odgovor = obradiKomanduNarudzba(poklapanjeNarudzba);
        } else if (poklapanjeJelo.matches()) {
          odgovor = obradiKomanduJelo(poklapanjeJelo);
        } else if (poklapanjePice.matches()) {
          odgovor = obradiKomanduPice(poklapanjePice);
        } else if (poklapanjeRacun.matches()) {
          odgovor = obradiKomanduRacun(poklapanjeRacun);
        } else if (poklapanjeStanje.matches()) {
          odgovor = obradiKomanduStanje(poklapanjeStanje);
        } else {
          odgovor = "ERROR 40 - Neispravna sintaksa komande\n";
        }
      } else {
        odgovor = "ERROR 48 - Poslužitelj za prijem zahtjeva kupaca u pauzi";
      }

      out.println(odgovor);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();

    } catch (Exception e) {

    }
  }


  /**
   * Metoda koja služi za dohvaćanje otvorenih narudžbi podataka te generira pripadajući odgovor.
   *
   * @param poklapanjeStanje objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduStanje(Matcher poklapanjeStanje) {
    try {
      String korisnik = poklapanjeStanje.group("korisnik");

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        return "ERROR 43 - Ne postoji otvorena narudžba za korisnika\n" + korisnik;
      }
      List<Narudzba> narudzbaStavke = otvoreneNarudzbe.get(korisnik);

      Gson gson = new Gson();
      String jsonStavke = gson.toJson(narudzbaStavke);

      return "OK\n" + jsonStavke + "\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande STANJE\n";
    }

  }

  /**
   * Metoda koja obrađuje zahtjev za dodavanjem pića u narudžbu te generira pripadajući odgovor.
   *
   * @param poklapanjePice objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduPice(Matcher poklapanjePice) {
    zakljucavanje.lock();
    try {
      String korisnik = poklapanjePice.group("korisnik");
      String idPica = poklapanjePice.group("idPica");
      float kolicina = Float.parseFloat(poklapanjePice.group("kolicina"));

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        return "ERROR 43 - Ne postoji otvorena narudžba za korisnika\n" + korisnik;
      }
      List<Narudzba> narudzbaStavke = otvoreneNarudzbe.get(korisnik);

      if (!kartaPica.containsKey(idPica)) {
        return "ERROR 42 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n";
      }
      KartaPica pice = kartaPica.get(idPica);

      Narudzba novaStavka = new Narudzba(korisnik, idPica, false, kolicina, pice.cijena(),
          System.currentTimeMillis());
      narudzbaStavke.add(novaStavka);
      return "OK\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande PIĆE\n";
    } finally {
      zakljucavanje.unlock();
    }
  }


  /**
   * Metoda koja obrađuje zahtjev za dodavanjem jela u narudžbu te generira pripadajući odgovor.
   *
   * @param poklapanjeJelo objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduJelo(Matcher poklapanjeJelo) {
    zakljucavanje.lock();
    try {
      String korisnik = poklapanjeJelo.group("korisnik");
      String idJela = poklapanjeJelo.group("idJela");
      float kolicina = Float.parseFloat(poklapanjeJelo.group("kolicina"));

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        return "ERROR 43 - Ne postoji otvorena narudžba za korisnika\n";
      }
      List<Narudzba> narudzbaStavke = otvoreneNarudzbe.get(korisnik);

      if (!jelovnik.containsKey(idJela)) {
        return "ERROR 41 - Ne postoji jelo s id u kolekciji jelovnika kod partnera\n";
      }
      Jelovnik jelo = jelovnik.get(idJela);

      Narudzba novaStavka =
          new Narudzba(korisnik, idJela, true, kolicina, jelo.cijena(), System.currentTimeMillis());
      narudzbaStavke.add(novaStavka);
      return "OK\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande JELO\n";
    } finally {
      zakljucavanje.unlock();
    }
  }


  /**
   * Metoda koja obrađuje zahtjev za kreiranjem nove narudžbe te generira pripadajući odgovor.
   *
   * @param poklapanjeNarudzba objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduNarudzba(Matcher poklapanjeNarudzba) {
    zakljucavanje.lock();
    try {
      String korisnik = poklapanjeNarudzba.group("korisnik");

      if (otvoreneNarudzbe.containsKey(korisnik)) {
        return "ERROR 44 - Već postoji otvorena narudžba za korisnika\n";
      }

      List<Narudzba> novaNarudzbaLista = new ArrayList<>();
      otvoreneNarudzbe.put(korisnik, novaNarudzbaLista);
      return "OK\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande NARUDŽBA\n";
    } finally {
      zakljucavanje.unlock();
    }
  }


  /**
   * Metoda koja obrađuje zahtjev za dohvaćanjem karte pića partnera te generira pripadajući
   * odgovor.
   *
   * @return odgovor (OK\n {jsonPopisKartaPica} - ako je sve uredu ili ERROR {opisPogreške} - ako se
   *         desila greška)
   */
  private String obradiKomanduKartaPica() {
    try {
      if (kartaPica.isEmpty()) {
        return "ERROR 47 - Neuspješno preuzimanje karte pića\n";
      }
      Gson gson = new Gson();
      String json = gson.toJson(kartaPica.values());

      return "OK\n" + json + "\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande KARTAPIĆA\n";
    }
  }


  /**
   * Metoda koja obrađuje zahtjev za dohvaćanjem jelovnika partnera te generira pripadajući odgovor.
   *
   * @return odgovor (OK\n {jsonPopisJelovnik} - ako je sve uredu ili ERROR {opisPogreške} - ako se
   *         desila greška)
   */
  private String obradiKomanduJelovnik() {
    try {
      if (jelovnik.isEmpty()) {
        return "ERROR 46 - Neuspješno preuzimanje jelovnika\n";
      }
      Gson gson = new Gson();
      String json = gson.toJson(jelovnik.values());

      return "OK\n" + json + "\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande JELOVNIK\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za kreiranjem računa te generira pripadajući odgovor.
   *
   * @param poklapanjeRacun objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu i ne šalje se obračun, OK\n {jsonNarudzbi} - ako se
   *         narudžbe šalju na obračun ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduRacun(Matcher poklapanjeRacun) {
    zakljucavanje.lock();
    try {
      String korisnik = poklapanjeRacun.group("korisnik");

      if (!otvoreneNarudzbe.containsKey(korisnik)) {
        return "ERROR 43 Ne postoji otvorena narudžba za korisnika\n";
      }
      List<Narudzba> narudzbaStavke = otvoreneNarudzbe.remove(korisnik);

      if (placeneNarudzbe.containsKey(korisnik)) {
        placeneNarudzbe.get(korisnik).addAll(narudzbaStavke);
      } else {
        placeneNarudzbe.put(korisnik, narudzbaStavke);
      }
      brojNaplacenihNarudzbi++;
      if (brojNaplacenihNarudzbi % kvotaNarudzbi == 0) {
        return posaljiKomanduObracun();
      }
      return "OK\n";
    } catch (Exception e) {
      return "ERROR 49 - Neuspješna obrada komande RAČUN\n";
    } finally {
      zakljucavanje.unlock();
    }
  }


  /**
   * Metoda koja obrađuje zahtjev za slanjem obračuna te generira pripadajući odgovor.
   *
   * @return odgovor (OK\n {jsonNarudzbi} - ako se narudžbe šalju na obračun ili ERROR
   *         {opisPogreške} - ako se desila greška)
   */
  private String posaljiKomanduObracun() {
    var jsonObracuni = kreirajJsonObracuna();
    var komandaObracun = kreirajKomanduObracun(jsonObracuni);
    var adresa = this.konfig.dajPostavku("adresa");
    var mreznaVrataRad = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    var odgovor = "";

    try {
      var mreznaUticnica = new Socket(adresa, mreznaVrataRad);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komandaObracun);
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      if (linija.equals("OK"))
        odgovor = "OK\n" + jsonObracuni + "\n";
      else
        odgovor = "ERROR 45 - Neuspješno slanje obračuna\n";
      mreznaUticnica.close();
    } catch (IOException e) {
      e.printStackTrace();
      odgovor = "ERROR 45 - Neuspješno slanje obračuna\n";
    }
    return odgovor;

  }


  /**
   * Metoda koja služi za kreiranjem JSONa obračuna na temelju plaćenih narudžbi.
   *
   * @return JSON narudžbi
   */
  private String kreirajJsonObracuna() {
    Map<String, Obracun> obracuniRijecnik = new ConcurrentHashMap<>();

    for (List<Narudzba> narudzbaLista : placeneNarudzbe.values()) {
      for (Narudzba nar : narudzbaLista) {
        String idStavke = nar.id();
        if (obracuniRijecnik.containsKey(idStavke)) {
          continue;
        }
        float ukupnaKolicina = 0.0f;
        for (List<Narudzba> lista : placeneNarudzbe.values()) {
          for (Narudzba n : lista) {
            if (n.id().equals(idStavke)) {
              ukupnaKolicina += n.kolicina();
            }
          }
        }
        int partnerId = Integer.parseInt(konfig.dajPostavku("id"));
        boolean jelo = nar.jelo();
        float cijena = nar.cijena();
        Obracun obracun = new Obracun(partnerId, idStavke, jelo, ukupnaKolicina, cijena,
            System.currentTimeMillis());
        obracuniRijecnik.put(idStavke, obracun);
      }
    }
    Gson gson = new Gson();
    return gson.toJson(obracuniRijecnik.values());
  }


  /**
   * Metoda koja služi za kreiranje komande koja se šalje za obračun.
   *
   * @param jsonObracuni JSON obračuna
   * @return komanda koja se šalje za obračun
   */
  private String kreirajKomanduObracun(String jsonObracuni) {
    String id = konfig.dajPostavku("id");
    String sigKod = konfig.dajPostavku("sigKod");
    StringBuilder komanda = new StringBuilder();
    komanda.append("OBRAČUN ").append(id).append(" ").append(sigKod).append("\n")
        .append(jsonObracuni).append("\n");

    return komanda.toString();
  }


  /**
   * Metoda koja služi za učitavanje jelovnika i karte pića partnera.
   *
   * @param adresa adresa na koju se šalju zahtjevi za jelovnikom i kartom pića
   * @param mreznaVrata mrežna vrata na koju se šalju zahtjevi za jelovnikom i kartom pića
   * @return vraća true ako je uspiješno učitavanje
   */
  private boolean ucitajJelovnikIKartuPica(String adresa, int mreznaVrata) {
    var komandaJelovnik = kreirajKomanduJelovnik();
    var komandaKartaPica = kreirajKomanduKartaPica();

    try (Socket mreznaUticnicaJelovnik = new Socket(adresa, mreznaVrata);
        BufferedReader inJelovnik = new BufferedReader(
            new InputStreamReader(mreznaUticnicaJelovnik.getInputStream(), "utf8"));
        PrintWriter outJelovnik = new PrintWriter(
            new OutputStreamWriter(mreznaUticnicaJelovnik.getOutputStream(), "utf8"), true)) {

      if (!posaljiKomanduJelovnik(mreznaUticnicaJelovnik, inJelovnik, outJelovnik,
          komandaJelovnik)) {
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    try (Socket mreznaUticnicaKartaPica = new Socket(adresa, mreznaVrata);
        BufferedReader inKartaPica = new BufferedReader(
            new InputStreamReader(mreznaUticnicaKartaPica.getInputStream(), "utf8"));
        PrintWriter outKartaPica = new PrintWriter(
            new OutputStreamWriter(mreznaUticnicaKartaPica.getOutputStream(), "utf8"), true)) {

      if (!posaljiKomanduKartaPica(mreznaUticnicaKartaPica, inKartaPica, outKartaPica,
          komandaKartaPica)) {
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }


  /**
   * Metoda koja služi za slanje zahtjeva za kartom pića.
   *
   * @param mreznaUticnica mrežna utičnica na koju se šalje zahtjev
   * @param in objekt tipa { @BufferedReader } za čitanje linija odgovora s mrežne utičnice
   * @param out objekt tipa { @PrintWriter } za slanje naredbe i podataka putem mrežne utičnice
   * @param komandaKartaPica the komanda koja se šalje kao zahtjev za kartom pića
   * @return vraća true, ako je uspiješno dohvaćanje
   * @throws IOException Signal koji ja javlja da se desila iznimka tipa { @code IOException }
   */
  private boolean posaljiKomanduKartaPica(Socket mreznaUticnica, BufferedReader in, PrintWriter out,
      String komandaKartaPica) throws IOException {
    out.write(komandaKartaPica);
    out.flush();
    mreznaUticnica.shutdownOutput();
    var odgovor = in.readLine();
    mreznaUticnica.shutdownInput();
    if (!odgovor.equals("OK")) {
      return false;
    }
    StringBuilder jsonOdgovor = new StringBuilder();
    String linija;
    boolean jsonZapocet = false;
    while ((linija = in.readLine()) != null) {
      if (!jsonZapocet && linija.contains("[")) {
        jsonZapocet = true;
      }
      if (jsonZapocet) {
        jsonOdgovor.append(linija);
        if (linija.contains("]") || !in.ready()) {
          break;
        }
      }
    }
    var kartaPicaOdgovor = jsonOdgovor.toString().trim();
    if (kartaPicaOdgovor.isEmpty()) {
      return false;
    }
    Gson gson = new Gson();
    var kartaPicaNiz = gson.fromJson(kartaPicaOdgovor, KartaPica[].class);
    var kartaPicaTok = Stream.of(kartaPicaNiz);
    kartaPicaTok.forEach(k -> kartaPica.put(k.id(), k));
    return true;
  }


  /**
   * Metoda koja služi za slanje zahtjeva za jelovnikom.
   *
   * @param mreznaUticnica mrežna utičnica na koju se šalje zahtjev
   * @param in objekt tipa { @BufferedReader } za čitanje linija odgovora s mrežne utičnice
   * @param out objekt tipa { @PrintWriter } za slanje naredbe i podataka putem mrežne utičnice
   * @param komandaJelovnik the komanda koja se šalje kao zahtjev za jelovnikom
   * @return vraća true, ako je uspiješno dohvaćanje
   * @throws IOException Signal koji ja javlja da se desila iznimka tipa { @code IOException }
   */
  private Boolean posaljiKomanduJelovnik(Socket mreznaUticnica, BufferedReader in, PrintWriter out,
      String komandaJelovnik) throws IOException {
    out.write(komandaJelovnik);
    out.flush();
    mreznaUticnica.shutdownOutput();
    var odgovor = in.readLine();
    mreznaUticnica.shutdownInput();
    if (!odgovor.equals("OK")) {
      return false;
    }
    StringBuilder jsonOdgovor = new StringBuilder();
    String linija;
    boolean jsonZapocet = false;
    while ((linija = in.readLine()) != null) {
      if (!jsonZapocet && linija.contains("[")) {
        jsonZapocet = true;
      }
      if (jsonZapocet) {
        jsonOdgovor.append(linija);
        if (linija.contains("]") || !in.ready()) {
          break;
        }
      }
    }
    var jelovnikOdgovor = jsonOdgovor.toString().trim();
    if (jelovnikOdgovor.isEmpty()) {
      return false;
    }
    Gson gson = new Gson();
    var jelovnikNiz = gson.fromJson(jelovnikOdgovor, Jelovnik[].class);
    var jelovnikTok = Stream.of(jelovnikNiz);
    jelovnikTok.forEach(j -> jelovnik.put(j.id(), j));
    return true;
  }

  /**
   * Metoda koja služi za kreiranje komande koja se šalje za kraj.
   *
   * @return komanda koja se šalje za kraj
   */
  private String kreirajKomanduKraj() {
    var kodZaKraj = this.konfig.dajPostavku("kodZaKraj");

    StringBuilder komanda = new StringBuilder();
    komanda.append("KRAJ ").append(kodZaKraj).append("\n");
    return komanda.toString();
  }

  /**
   * Metoda koja služi za kreiranje komande koja se šalje za dohvaćanje karte pića.
   *
   * @return komanda koja se šalje za dohvaćanje karte pića
   */
  private String kreirajKomanduKartaPica() {
    String id = this.konfig.dajPostavku("id");
    String sigKod = this.konfig.dajPostavku("sigKod");

    StringBuilder komanda = new StringBuilder();
    komanda.append("KARTAPIĆA ").append(id).append(" ").append(sigKod).append("\n");

    return komanda.toString();
  }


  /**
   * Metoda koja služi za kreiranje komande koja se šalje za dohvaćanje jelovnika.
   *
   * @return komanda koja se šalje za dohvaćanje jelovnika
   */
  private String kreirajKomanduJelovnik() {
    String id = this.konfig.dajPostavku("id");
    String sigKod = this.konfig.dajPostavku("sigKod");

    StringBuilder komanda = new StringBuilder();
    komanda.append("JELOVNIK ").append(id).append(" ").append(sigKod).append("\n");

    return komanda.toString();
  }


  /**
   * Metoda koja služi za kreiranje komande koja se šalje za registraciju partnera.
   *
   * @return komanda koja se šalje za registraciju partnera
   */
  private String kreirajKomanduRegistracijaPartnera() {
    String id = this.konfig.dajPostavku("id");
    String naziv = this.konfig.dajPostavku("naziv");
    String vrstaKuhinje = this.konfig.dajPostavku("kuhinja");
    String adresa = this.konfig.dajPostavku("adresa");
    String mreznaVrata = this.konfig.dajPostavku("mreznaVrata");
    String gpsSirina = this.konfig.dajPostavku("gpsSirina");
    String gpsDuzina = this.konfig.dajPostavku("gpsDuzina");
    String mreznaVrataKraj = this.konfig.dajPostavku("mreznaVrataKrajPartner");
    String adminKod = this.konfig.dajPostavku("kodZaAdmin");


    StringBuilder komanda = new StringBuilder();
    komanda.append("PARTNER ").append(id).append(" \"").append(naziv).append("\" ")
        .append(vrstaKuhinje).append(" ").append(adresa).append(" ").append(mreznaVrata).append(" ")
        .append(gpsSirina).append(" ").append(gpsDuzina).append(" ").append(mreznaVrataKraj)
        .append(" ").append(adminKod).append("\n");

    return komanda.toString();
  }

  /**
   * Metoda koja služi za učitavanje konfiguracijske datoteke.
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
