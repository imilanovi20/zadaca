package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.konfiguracije.Konfiguracija;
import edu.unizg.foi.nwtis.konfiguracije.KonfiguracijaApstraktna;
import edu.unizg.foi.nwtis.konfiguracije.NeispravnaKonfiguracija;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;


/**
 * Klasa PosluziteljTvrtka koja je odgovorna za pokretanje 3 virtualne dretve na kojima se pokreću
 * poslužitelji za: kraj rada, registraciju partnera i rad s partnerima.
 * 
 * @author Ivan Milanović-Litre
 * @version 1.5.0
 */
public class PosluziteljTvrtka {

  /** Konfiguracijski podaci. */
  protected Konfiguracija konfig;

  /** Pokretač dretvi. */
  protected ExecutorService izvrsitelj = null;

  /** Pauza dretve. */
  private int pauzaDretve = 1000;

  /** Kod za kraj rada. */
  protected String kodZaKraj = "";

  /** The kod za admin tvrtke. */
  protected String kodZaAdminTvrtke = "";

  /** Zastavica za kraj rada. */
  protected AtomicBoolean kraj = new AtomicBoolean(false);

  /** Pauza registracija. */
  protected AtomicBoolean pauzaRegistracija = new AtomicBoolean(false);

  /** Pauza partneri. */
  protected AtomicBoolean pauzaPartneri = new AtomicBoolean(false);

  /** Prekinute veze na dretvi kraj. */
  protected AtomicInteger prekinutoKraj = new AtomicInteger(0);

  /** Prekinute veze na dretvi registracija. */
  protected AtomicInteger prekinutoRegistracija = new AtomicInteger(0);

  /** Prekinute veze na dretvi rad. */
  protected AtomicInteger prekinutoRad = new AtomicInteger(0);

  /** Zakljucavanje dretvi prilikom obrade pojedinih komandi. */
  private static Lock zakljucavanje = new ReentrantLock();

  /** Aktivne dretve. */
  protected Queue<Future<?>> aktivneDretve = new ConcurrentLinkedQueue<>();

  /** Kuhinje. */
  protected Map<String, String> kuhinje = new ConcurrentHashMap<>();

  /** Jelovnici. */
  protected Map<String, Map<String, Jelovnik>> jelovnici = new ConcurrentHashMap<>();

  /** Karta pića. */
  protected Map<String, KartaPica> kartaPica = new ConcurrentHashMap<>();

  /** Partneri. */
  protected Map<Integer, Partner> partneri = new ConcurrentHashMap<>();

  /** The putanja rest. */
  protected String putanjaRest = "";

  /** The klijent. */
  protected HttpClient klijent = HttpClient.newHttpClient();

  /** Predložak kraj. */
  private Pattern predlozakKraj = Pattern.compile("^KRAJ (?<kod>\\w+)$");

  /** Predložak za učitavanje kuhinje. */
  private Pattern predlozakUcitavanjeKuhinje = Pattern.compile("^kuhinja_[1-9]$");

  /** Predložak vrijednosti kuhinje. */
  private Pattern predlozakVrijednostiKuhinje = Pattern.compile("^(?<oznaka>\\w+);(?<naziv>.+)$");

  /** Predlozak dodavanje partnera. */
  protected Pattern predlozakDodajPartnera = Pattern.compile(
      "^PARTNER\\s+(?<id>\\d+)\\s+\"(?<naziv>[^\"]+)\"\\s+(?<vrstaKuhinje>\\w+)\\s+(?<adresa>\\S+)\\s+(?<mreznaVrata>\\d+)\\s+(?<gpsSirina>\\d+\\.\\d+)\\s+(?<gpsDuzina>\\d+\\.\\d+)\\s+(?<mreznaVrataKraj>\\d+)\\s+(?<adminKod>\\w+)$");

  /** Predložak brisanje partnera. */
  protected Pattern predlozakObrisiPartnera =
      Pattern.compile("^OBRIŠI\\s+(?<id>\\d+)\\s+(?<sigurnosniKod>\\S+)$");

  /** Pedložak popis partnera. */
  protected Pattern predlozakPopisPartnera = Pattern.compile("^POPIS$");

  /** Pedložak jelovnik. */
  protected Pattern predlozakJelovnik =
      Pattern.compile("^JELOVNIK\\s+(?<id>\\d+)\\s+(?<sigurnosniKod>\\S+)$");

  /** Predložak kartapica. */
  protected Pattern predlozakKartapica =
      Pattern.compile("^KARTAPIĆA\\s+(?<id>\\d+)\\s+(?<sigurnosniKod>\\S+)$");

  /** Pedložak obračun. */
  protected Pattern predlozakObracun =
      Pattern.compile("^OBRAČUN\\s+(?<id>\\d+)\\s+(?<sigurnosniKod>\\S+)$");

  /** Predlozak status. */
  protected Pattern predlozakStatus = Pattern.compile("^STATUS\\s+(?<kod>\\w+)\\s+(?<dio>[12])$");

  /** Predložak pauza. */
  protected Pattern predlozakPauza = Pattern.compile("^PAUZA\\s+(?<kod>\\w+)\\s+(?<dio>[12])$");

  /** Predložak start. */
  protected Pattern predlozakStart = Pattern.compile("^START\\s+(?<kod>\\w+)\\s+(?<dio>[12])$");

  /** Predložak spava. */
  protected Pattern predlozakSpava =
      Pattern.compile("^SPAVA\\s+(?<kod>\\w+)\\s+(?<spavanje>\\d+)$");

  /** Predložak osvjezi. */
  protected Pattern predlozakOsvjezi = Pattern.compile("^OSVJEŽI\\s+(?<kod>\\w+)$");

  /** Predložak kraj WS. */
  protected Pattern predlozakKrajWS = Pattern.compile("^KRAJWS\\s+(?<kod>\\w+)$");

  /** Predložak obracun WS. */
  protected Pattern predlozakObracunWS =
      Pattern.compile("^OBRAČUNWS\\s+(?<id>\\d+)\\s+(?<sigurnosniKod>\\S+)$");

  /**
   * Gets the konfig.
   *
   * @return the konfig
   */
  public Konfiguracija getKonfig() {
    return konfig;
  }

  /**
   * Glavna metoda programa.
   *
   * @param args niz argumenata koji se proslijeduju u komandnoj liniji prilikom pokretanja
   *        programa.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Broj argumenata nije 1.");
      return;
    }

    var program = new PosluziteljTvrtka();
    program.registrirajPrislinoGasenje();
    var nazivDatoteke = args[0];
    program.pripremiKreni(nazivDatoteke);
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
      System.out.println("Prekinuto(dretva kraj): " + this.prekinutoKraj.get());
      System.out
          .println("Prekinuto(dretva registracija partnera): " + this.prekinutoRegistracija.get());
      System.out.println("Prekinuto(dretva rad s partnerima): " + this.prekinutoRad.get());
      System.out.println("Broj prekinutih dretvi: " + brojPrekinutihDretvi);
    }));
  }

  /**
   * Metoda koja služi za kreiranje potrebnih dretvi te pokretanje pripadajućih poslužitelja.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   */
  public void pripremiKreni(String nazivDatoteke) {
    if (!this.ucitajKonfiguraciju(nazivDatoteke) || !this.ucitajPartnere() || !this.ucitajKuhinje()
        || !this.ucitajKartuPica()) {
      return;
    }
    this.kodZaKraj = this.konfig.dajPostavku("kodZaKraj");
    this.pauzaDretve = Integer.parseInt(this.konfig.dajPostavku("pauzaDretve"));

    this.kodZaAdminTvrtke = this.konfig.dajPostavku("kodZaAdminTvrtke");
    this.putanjaRest = this.konfig.dajPostavku("restAdresa");

    var graditelj = Thread.ofVirtual();
    var tvornicaDretvi = graditelj.factory();
    this.izvrsitelj = Executors.newThreadPerTaskExecutor(tvornicaDretvi);

    var dretvaZaKraj = this.izvrsitelj.submit(() -> this.pokreniPosluziteljKraj());
    aktivneDretve.add(dretvaZaKraj);
    var dretvaZaRegistraciju = this.izvrsitelj.submit(() -> this.pokreniPosluziteljRegistracija());
    aktivneDretve.add(dretvaZaRegistraciju);
    var dretvaZaRad = this.izvrsitelj.submit(() -> this.pokreniPosluziteljRad());
    aktivneDretve.add(dretvaZaRad);

    while (!dretvaZaKraj.isDone() && !dretvaZaRegistraciju.isDone() && !dretvaZaRad.isDone()) {
      try {
        Thread.sleep(this.pauzaDretve);
      } catch (InterruptedException e) {

      }
    }

    if (kraj.get()) {
      for (var dretva : aktivneDretve) {
        if (!dretva.isDone()) {
          dretva.cancel(true);
        }
      }
    }

    this.izvrsitelj.shutdown();
  }

  /**
   * Metoda koja služi za pokretanje poslužitelja za kraj rada programa.
   */
  public void pokreniPosluziteljKraj() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataKraj"));
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
   * Metoda koja služi za pokretanje poslužitelja za registraciju partnera.
   */
  public void pokreniPosluziteljRegistracija() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRegistracija"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          var dretva = this.izvrsitelj.submit(() -> obradiRegistracijuPartnera(mreznaUticnica));
          this.aktivneDretve.add(dretva);
        } catch (Exception e) {
          if (!this.kraj.get())
            this.prekinutoRegistracija.incrementAndGet();
        }
      }
      ss.close();
    } catch (IOException e) {
    }
  }

  /**
   * Metoda koja služi za pokretanje poslužitelja za rad s partnerima.
   */
  public void pokreniPosluziteljRad() {
    var mreznaVrata = Integer.parseInt(this.konfig.dajPostavku("mreznaVrataRad"));
    var brojCekaca = Integer.parseInt(this.konfig.dajPostavku("brojCekaca"));
    try (ServerSocket ss = new ServerSocket(mreznaVrata, brojCekaca)) {
      while (!this.kraj.get()) {
        try {
          var mreznaUticnica = ss.accept();
          var dretva = this.izvrsitelj.submit(() -> obradiRad(mreznaUticnica));
          this.aktivneDretve.add(dretva);
        } catch (Exception e) {
          if (!this.kraj.get())
            this.prekinutoRad.incrementAndGet();
        }
      }
      ss.close();
    } catch (IOException e) {
    }
  }

  /**
   * Metoda koja obrađuje zahtjev koji je poslan na poslužitelj za kraj te ukoliko je poslan
   * ispravan zahtjev za kraj postavlja zastavicu kraj na true.
   *
   * @param mreznaUticnica mrežna utičnica poslužitelja za kraj
   * @return vraća true ako je sve prošlo uredu
   */
  public Boolean obradiKraj(Socket mreznaUticnica) {
    PrintWriter out = null;
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      String linija = in.readLine();
      mreznaUticnica.shutdownInput();

      Matcher poklapanjeKraj = predlozakKraj.matcher(linija.trim());
      Matcher poklapanjeStatus = predlozakStatus.matcher(linija.trim());
      Matcher poklapanjePauza = predlozakPauza.matcher(linija.trim());
      Matcher poklapanjeStart = predlozakStart.matcher(linija.trim());
      Matcher poklapanjeSpava = predlozakSpava.matcher(linija.trim());
      Matcher poklapanjeOsvjezi = predlozakOsvjezi.matcher(linija.trim());
      Matcher poklapanjeKrajWS = predlozakKrajWS.matcher(linija.trim());
      String odgovor = null;

      odgovor = obradiKomandeKraj(poklapanjeKraj, poklapanjeStatus, poklapanjePauza,
          poklapanjeStart, poklapanjeSpava, poklapanjeOsvjezi, poklapanjeKrajWS);
      out.println(odgovor);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();
    } catch (Exception e) {
      if (out != null) {
        out.write("ERROR 19 - Neuspješna obrada komande KRAJ\n");
        out.flush();
      }
    }
    return Boolean.TRUE;
  }

  /**
   * Obradi komande kraj.
   *
   * @param poklapanjeKraj poklapanje kraj
   * @param poklapanjeStatus poklapanje status
   * @param poklapanjePauza poklapanje pauza
   * @param poklapanjeStart poklapanje start
   * @param poklapanjeSpava poklapanje spava
   * @param poklapanjeOsvjezi poklapanje osvjezi
   * @param poklapanjeKrajWS poklapanje kraj WS
   * @return odgovor komande
   */
  private String obradiKomandeKraj(Matcher poklapanjeKraj, Matcher poklapanjeStatus,
      Matcher poklapanjePauza, Matcher poklapanjeStart, Matcher poklapanjeSpava,
      Matcher poklapanjeOsvjezi, Matcher poklapanjeKrajWS) {
    String odgovor;
    if (poklapanjeKraj.matches()) {
      odgovor = obradiKomanduKraj(poklapanjeKraj, true);
    } else if (poklapanjeStatus.matches()) {
      odgovor = obradiKomanduStatus(poklapanjeStatus);
    } else if (poklapanjePauza.matches()) {
      odgovor = obradiKomanduPauza(poklapanjePauza);
    } else if (poklapanjeStart.matches()) {
      odgovor = obradiKomanduStart(poklapanjeStart);
    } else if (poklapanjeSpava.matches()) {
      odgovor = obradiKomanduSpava(poklapanjeSpava);
    } else if (poklapanjeOsvjezi.matches()) {
      odgovor = obradiKomanduOsvjezi(poklapanjeOsvjezi);
    } else if (poklapanjeKrajWS.matches()) {
      odgovor = obradiKomanduKraj(poklapanjeKrajWS, false);
    } else {
      odgovor = "ERROR 10 - Format komande nije ispravan\n";
    }
    return odgovor;
  }

  /**
   * Obradi komandu kraj.
   *
   * @param poklapanjeKraj objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @param posaljiRest posalji rest
   * @return odgovor (OK} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduKraj(Matcher poklapanjeKraj, boolean posaljiRest) {
    try {
      String uneseniKod = poklapanjeKraj.group("kod");
      if (uneseniKod.equals(this.kodZaKraj)) {
        boolean sviParteriZavrseni = posaljiKrajSvimPartnerima();
        if (sviParteriZavrseni) {
          if (posaljiRest) {
            boolean restUspjesan = posaljiKrajNaRest(uneseniKod);
            if (!restUspjesan) {
              return "ERROR 17 - RESTful zahtjev nije uspješan\n";
            }
          }
          this.kraj.set(true);
          return "OK\n";
        } else {
          return "ERROR 14 - Barem jedan partner nije završio rad\n";
        }
      } else {
        return "ERROR 10 - Pogrešan kod za kraj\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande OSVJEŽI\n";
    }
  }


  /**
   * Posalji kraj svim partnerima.
   *
   * @return true ako je uspiješno
   */
  private boolean posaljiKrajSvimPartnerima() {
    for (Partner partner : partneri.values()) {
      try {
        String adresa = partner.adresa();
        int mreznaVrata = partner.mreznaVrataKraj();

        try (Socket mreznaUticnica = new Socket(adresa, mreznaVrata)) {
          PrintWriter out =
              new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
          BufferedReader in =
              new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));

          String komanda = kreirajKomanduKraj();
          out.write(komanda);
          out.flush();
          mreznaUticnica.shutdownOutput();

          String odgovor = in.readLine();
          if (odgovor == null || !odgovor.equals("OK")) {
            return false;
          }
        }
      } catch (Exception e) {
      }
    }
    return true;
  }

  /**
   * Posalji kraj na rest.
   *
   * @param uneseniKod uneseni kod
   * @return true ako je uspiješno
   */
  private boolean posaljiKrajNaRest(String uneseniKod) {
    try {
      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(putanjaRest + "/kraj/info"))
          .method("HEAD", HttpRequest.BodyPublishers.noBody()).header("Accept", "application/json")
          .build();

      HttpResponse<String> odgovor =
          this.klijent.send(zahtjev, HttpResponse.BodyHandlers.ofString());
      return odgovor.statusCode() == 200;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Obradi komandu osvjezi.
   *
   * @param poklapanjeOsvjezi objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduOsvjezi(Matcher poklapanjeOsvjezi) {
    try {
      String uneseniKod = poklapanjeOsvjezi.group("kod");
      if (uneseniKod.equals(this.kodZaAdminTvrtke)) {
        if (!this.pauzaPartneri.get()) {
          if (this.ucitajKartuPica() && this.ucitajKuhinje()) {
            return "OK\n";
          } else {
            return "ERROR 19 - Problem kod osvježavanja podataka\n";
          }
        } else {
          return "ERROR 15 - Poslužitelj za partnere u pauzi\n";
        }
      } else {
        return "ERROR 12 - Nije ispravan admin kod\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande OSVJEŽI\n";
    }
  }

  /**
   * Obradi komandu spava.
   *
   * @param poklapanjeSpava objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduSpava(Matcher poklapanjeSpava) {
    try {
      String kod = poklapanjeSpava.group("kod");
      int spavanje = Integer.parseInt(poklapanjeSpava.group("spavanje"));

      if (!kod.equals(this.kodZaAdminTvrtke)) {
        return "ERROR 12 - Nije ispravan admin kod\n";
      }
      try {
        Thread.sleep(spavanje);
        return "OK\n";
      } catch (InterruptedException e) {
        return "ERROR 16 - Prekid spavanja dretve\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande SPAVA\n";
    }
  }

  /**
   * Obradi komandu start.
   *
   * @param poklapanjeStart objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduStart(Matcher poklapanjeStart) {
    try {
      String uneseniKod = poklapanjeStart.group("kod");
      String dio = poklapanjeStart.group("dio");

      if (uneseniKod.equals(this.kodZaAdminTvrtke)) {
        if (dio.equals("1")) {
          if (this.pauzaRegistracija.get()) {
            this.pauzaRegistracija.set(false);
            return "OK\n";
          } else {
            return "ERROR 13 - Pogrešna promjena pauze ili starta\n";
          }
        } else if (dio.equals("2")) {
          if (this.pauzaPartneri.get()) {
            this.pauzaPartneri.set(false);
            return "OK\n";
          } else {
            return "ERROR 13 - Pogrešna promjena pauze ili starta\n";
          }
        } else {
          return "ERROR 19 - Nepoznati dio poslužitelja\n";
        }
      } else {
        return "ERROR 12 - Pogrešan kodZaAdminTvrtke\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande START\n";
    }
  }

  /**
   * Obradi komandu pauza.
   *
   * @param poklapanjePauza objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduPauza(Matcher poklapanjePauza) {
    try {
      String uneseniKod = poklapanjePauza.group("kod");
      String dio = poklapanjePauza.group("dio");

      if (uneseniKod.equals(this.kodZaAdminTvrtke)) {
        if (dio.equals("1")) {
          if (!this.pauzaRegistracija.get()) {
            this.pauzaRegistracija.set(true);
            return "OK\n";
          } else {
            return "ERROR 13 - Pogrešna promjena pauze ili starta\n";
          }
        } else if (dio.equals("2")) {
          if (!this.pauzaPartneri.get()) {
            this.pauzaPartneri.set(true);
            return "OK\n";
          } else {
            return "ERROR 13 - Pogrešna promjena pauze ili starta\n";
          }
        } else {
          return "ERROR 19 - Nepoznati dio poslužitelja\n";
        }
      } else {
        return "ERROR 12 - Pogrešan kodZaAdminTvrtke\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande PAUZA\n";
    }
  }

  /**
   * Obradi komandu status.
   *
   * @param poklapanjeStatus objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK[0,1]} - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  private String obradiKomanduStatus(Matcher poklapanjeStatus) {
    try {
      String uneseniKod = poklapanjeStatus.group("kod");
      String dio = poklapanjeStatus.group("dio");

      if (uneseniKod.equals(this.kodZaAdminTvrtke)) {
        if (dio.equals("1")) {
          return "OK " + (this.pauzaRegistracija.get() ? "0" : "1") + "\n";
        } else if (dio.equals("2")) {
          return "OK " + (this.pauzaPartneri.get() ? "0" : "1") + "\n";
        } else {
          return "ERROR 19 - Nepoznati dio poslužitelja\n";
        }
      } else {
        return "ERROR 12 - Pogrešan kodZaAdminTvrtke\n";
      }
    } catch (Exception e) {
      return "ERROR 19 - Neuspješna obrada komande STATUS\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev koji je poslan na poslužitelj za registraciju partnera, za početak
   * provjerava zahtjev koji je poslan te na temelju njega poziva pripadajuću metodu te kreira
   * odgovor na zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica poslužitelja za registraciju partnera
   */
  public void obradiRegistracijuPartnera(Socket mreznaUticnica) {
    PrintWriter out = null;
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();

      Matcher poklapanjeDodajPartnera = predlozakDodajPartnera.matcher(linija.trim());
      Matcher poklapanjeObrisiPartnera = predlozakObrisiPartnera.matcher(linija.trim());
      Matcher poklapanjePopisPartnera = predlozakPopisPartnera.matcher(linija.trim());

      String odgovor = null;
      if (this.pauzaRegistracija.get()) {
        odgovor = "ERROR 24 – Poslužitelj za registraciju partnera u pauzi";
      } else {
        if (poklapanjeDodajPartnera.matches()) {
          odgovor = obradiKomanduDodajPartnera(poklapanjeDodajPartnera);
        } else if (poklapanjeObrisiPartnera.matches()) {
          odgovor = obradiKomanduObrisi(poklapanjeObrisiPartnera);
        } else if (poklapanjePopisPartnera.matches()) {
          odgovor = obradiKomanduPopis();
        } else {
          odgovor = "ERROR 20 - Neispravna sintaksa komande\n";
        }
      }


      out.println(odgovor);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za popisom partnera te generira pripadajući odgovor.
   *
   * @return odgovor (OK\n {jsonPopisaPartnera} - ako je sve uredu ili ERROR {opisPogreške} - ako se
   *         desila greška)
   */
  public String obradiKomanduPopis() {
    try {
      Gson gson = new Gson();
      List<PartnerPopis> popisPartnera = partneri.values().stream()
          .map(partner -> new PartnerPopis(partner.id(), partner.naziv(), partner.vrstaKuhinje(),
              partner.adresa(), partner.mreznaVrata(), partner.gpsSirina(), partner.gpsDuzina()))
          .collect(Collectors.toList());

      String json = gson.toJson(popisPartnera);
      return "OK\n" + json + "\n";
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR 29 Nešto drugo nije u redu\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za brisanjem partnera te generira pripadajući odgovor.
   *
   * @param poklapanjeObrisiPartnera objekt tipa { @code Matcher } (podudarač) s pripadajućim
   *        grupama potrebnim za obradu zahtjeva
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  public String obradiKomanduObrisi(Matcher poklapanjeObrisiPartnera) {
    try {
      int id = Integer.parseInt(poklapanjeObrisiPartnera.group("id"));
      String uneseniSigurnosniKod = poklapanjeObrisiPartnera.group("sigurnosniKod");

      if (!partneri.containsKey(id)) {
        return "ERROR 23 - Ne postoji partner s id u kolekciji partnera\n";
      }
      Partner partner = partneri.get(id);
      if (!partner.sigurnosniKod().equals(uneseniSigurnosniKod)) {
        return "ERROR 22 - Neispravan sigurnosni kod partnera\n";
      }
      partneri.remove(id);
      spremiPartnere();
      return "OK\n";
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR 20 - Neuspješno brisanje partnera\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za dodavanjem partnera te generira pripadajući odgovor.
   *
   * @param poklapanjePartner objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK {sigurnosniKOdPartnera} - ako je sve uredu ili ERROR {opisPogreške} - ako
   *         se desila greška)
   */
  public String obradiKomanduDodajPartnera(Matcher poklapanjePartner) {
    try {
      int id = Integer.parseInt(poklapanjePartner.group("id"));
      String naziv = poklapanjePartner.group("naziv");
      String vrstaKuhinje = poklapanjePartner.group("vrstaKuhinje");
      String adresa = poklapanjePartner.group("adresa");
      int mreznaVrata = Integer.parseInt(poklapanjePartner.group("mreznaVrata"));
      float gpsSirina = Float.parseFloat(poklapanjePartner.group("gpsSirina"));
      float gpsDuzina = Float.parseFloat(poklapanjePartner.group("gpsDuzina"));
      int mreznaVrataKraj = Integer.parseInt(poklapanjePartner.group("mreznaVrataKraj"));
      String adminKod = poklapanjePartner.group("adminKod");

      if (partneri.containsKey(id)) {
        return "ERROR 21 - Već postoji partner s id u kolekciji partnera\n";
      }
      if (!kuhinje.containsKey(vrstaKuhinje)) {
        return "ERROR 29 - Registracija za nepostojeću kuhinju\n";
      }
      String sigurnosniKod = Integer.toHexString((naziv + adresa).hashCode());
      Partner noviPartner = new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata,
          mreznaVrataKraj, gpsSirina, gpsDuzina, sigurnosniKod, adminKod);
      partneri.put(id, noviPartner);
      if (!spremiPartnere()) {
        return "ERROR 29 - Neuspješno spremanje partnera u datoteku\n";
      }
      return "OK " + sigurnosniKod + "\n";
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR 20 - Neuspješno dodavanje partnera\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev koji je poslan na poslužitelj za rad s partnera+ima, za početak
   * provjerava zahtjev koji je poslan te na temelju njega poziva pripadajuću metodu te kreira
   * odgovor na zahtjev.
   *
   * @param mreznaUticnica mrežna utičnica poslužitelja za rad s partnerima
   */
  public void obradiRad(Socket mreznaUticnica) {
    PrintWriter out = null;
    try {
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      out = new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));

      String linija = in.readLine();

      Matcher poklapanjeJelovnik = predlozakJelovnik.matcher(linija.trim());
      Matcher poklapanjeKartaPica = predlozakKartapica.matcher(linija.trim());
      Matcher poklapanjeObracun = predlozakObracun.matcher(linija.trim());
      Matcher poklapanjeObracunWS = predlozakObracunWS.matcher(linija.trim());

      String odgovor = null;
      if (this.pauzaPartneri.get()) {
        odgovor = "ERROR 36 – Poslužitelj za partnere u pauzi";
      } else {
        if (poklapanjeJelovnik.matches()) {
          odgovor = obradiKomanduJelovnik(poklapanjeJelovnik);
        } else if (poklapanjeKartaPica.matches()) {
          odgovor = obradiKomanduKartaPica(poklapanjeKartaPica);
        } else if (poklapanjeObracun.matches()) {
          odgovor = obradiKomanduObracun(poklapanjeObracun, in, true);
        } else if (poklapanjeObracunWS.matches()) {
          odgovor = obradiKomanduObracun(poklapanjeObracunWS, in, false);
        } else {
          odgovor = "ERROR 30 - Format komande nije ispravan\n";
        }
      }

      out.println(odgovor);
      out.flush();
      mreznaUticnica.shutdownOutput();
      mreznaUticnica.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za spremanjem obračuna te generira pripadajući odgovor.
   *
   * @param poklapanjeObracun objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @param in JSON zapis obračuna
   * @param posaljiRest the posalji rest
   * @return odgovor (OK - ako je sve uredu ili ERROR {opisPogreške} - ako se desila greška)
   */
  public String obradiKomanduObracun(Matcher poklapanjeObracun, BufferedReader in,
      boolean posaljiRest) {
    zakljucavanje.lock();
    try {
      int partnerId = Integer.parseInt(poklapanjeObracun.group("id"));
      String uneseniSigurnosniKod = poklapanjeObracun.group("sigurnosniKod");
      if (!partneri.containsKey(partnerId)) {
        return "ERROR 31 - Ne postoji partner s tim id-om\n";
      }
      Partner partner = partneri.get(partnerId);
      if (!partner.sigurnosniKod().equals(uneseniSigurnosniKod)) {
        return "ERROR 31 - Neispravan sigurnosni kod partnera\n";
      }
      StringBuilder jsonObracun = new StringBuilder();
      String line;
      while ((line = in.readLine()) != null) {
        jsonObracun.append(line);
        if (line.contains("]") || !in.ready()) {
          break;
        }
      }
      String jsonPodaci = jsonObracun.toString();
      if (!(jsonPodaci.startsWith("[") || !jsonPodaci.endsWith("]")))
        return "ERROR 39 - Neispravan format JSON-a\n";
      Gson gson = new Gson();
      Obracun[] noviObracuni = gson.fromJson(jsonPodaci, Obracun[].class);
      for (Obracun o : noviObracuni) {
        if (o.id() == null)
          return "ERROR 35 - Neispravan obračun\n";
      }
      if (!spremiObracun(gson, noviObracuni)) {
        return "ERROR 35 - Neispravan obračun\n";
      }
      if (posaljiRest) {
        boolean uspjeh = posaljiObracunNaRest(noviObracuni, partner);
        if (!uspjeh) {
          return "ERROR 36 - RESTful zahtjev nije uspješan\n";
        }
      }
      return "OK\n";
    } catch (Exception e) {
      e.printStackTrace();
      return "ERROR 39 - Neuspješna obrada komande OBRAČUN\n";
    } finally {
      zakljucavanje.unlock();
    }
  }


  /**
   * Posalji obracun na rest.
   *
   * @param noviObracuni the novi obracuni
   * @param partnerObracuni the partner obracuni
   * @return true, if successful
   */
  private boolean posaljiObracunNaRest(Obracun[] noviObracuni, Partner partnerObracuni) {
    try {
      Gson gson = new Gson();
      String jsonPodaci = gson.toJson(noviObracuni);

      HttpRequest zahtjev = HttpRequest.newBuilder().uri(URI.create(putanjaRest + "/obracun"))
          .POST(HttpRequest.BodyPublishers.ofString(jsonPodaci))
          .header("Content-Type", "application/json").header("Accept", "application/json").build();

      HttpResponse<String> odgovor =
          this.klijent.send(zahtjev, HttpResponse.BodyHandlers.ofString());
      return odgovor.statusCode() == 201;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za dohvaćanje karte pića te generira pripadajući odgovor.
   *
   * @param poklapanjeKartaPica objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK {jsonPopisKartePića} - ako je sve uredu ili ERROR {opisPogreške} - ako se
   *         desila greška)
   */
  public String obradiKomanduKartaPica(Matcher poklapanjeKartaPica) {
    try {
      int partnerId = Integer.parseInt(poklapanjeKartaPica.group("id"));
      String uneseniSigurnosniKod = poklapanjeKartaPica.group("sigurnosniKod");

      if (!partneri.containsKey(partnerId)) {
        return "ERROR 31 - Ne postoji partner s tim id-om\n";
      }
      Partner partner = partneri.get(partnerId);
      if (!partner.sigurnosniKod().equals(uneseniSigurnosniKod)) {
        return "ERROR 31 - Neispravan sigurnosni kod partnera\n";
      }
      if (this.kartaPica.isEmpty()) {
        return "ERROR 39 - Ne postoji karta pića\n";
      }
      Gson gson = new Gson();
      String json = gson.toJson(kartaPica.values());

      return "OK\n" + json + "\n";
    } catch (Exception ex) {
      ex.printStackTrace();
      return "ERROR 39 - Greška prilikom obrade komande jelovnik\n";
    }
  }

  /**
   * Metoda koja obrađuje zahtjev za dohvaćanje jelovnika te generira pripadajući odgovor.
   *
   * @param poklapanjeJelovnik objekt tipa { @code Matcher } (podudarač) s pripadajućim grupama
   *        potrebnim za obradu zahtjeva
   * @return odgovor (OK {jsonPopisJelovnika} - ako je sve uredu ili ERROR {opisPogreške} - ako se
   *         desila greška)
   */
  public String obradiKomanduJelovnik(Matcher poklapanjeJelovnik) {
    try {
      int partnerId = Integer.parseInt(poklapanjeJelovnik.group("id"));
      String uneseniSigurnosniKod = poklapanjeJelovnik.group("sigurnosniKod");

      if (!partneri.containsKey(partnerId)) {
        return "ERROR 31 - Ne postoji partner s tim id-om\n";
      }
      Partner partner = partneri.get(partnerId);
      if (!partner.sigurnosniKod().equals(uneseniSigurnosniKod)) {
        return "ERROR 31 - Neispravan sigurnosni kod partnera\n";
      }

      String vrstaKuhinje = partner.vrstaKuhinje();
      if (!jelovnici.containsKey(vrstaKuhinje)) {
        return "ERROR 32 - Ne postoji jelovnik s vrstom kuhinje\n";
      }
      Map<String, Jelovnik> jelovnikRijecnik = jelovnici.get(vrstaKuhinje);
      Gson gson = new Gson();
      String jsonJelovnik = gson.toJson(jelovnikRijecnik.values());
      if (jelovnikRijecnik == null || jelovnikRijecnik.isEmpty()) {
        return "ERROR 39 - Prazan jelovnik za vrstu kuhinje\n";
      }

      return "OK\n" + jsonJelovnik + "\n";
    } catch (Exception e) {
      return "ERROR 39 - Greška prilikom obrade komande jelovnik\n";
    }
  }

  /**
   * Metoda koja služi za učitavanje partnera iz pripadajućih datoteka na početku rada programa.
   *
   * @return vraća true ako je uspiješno učitavanje
   */
  public boolean ucitajPartnere() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaPartnera");
    if (nazivDatoteke == null) {
      return false;
    }
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka)) {
      try {
        Files.createFile(datoteka);
        try (Writer writer = Files.newBufferedWriter(datoteka, StandardCharsets.UTF_8)) {
          writer.write("[]");
          return true;
        }
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      }
    }
    if (!Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      String sarzaj = Files.readString(datoteka, StandardCharsets.UTF_8);
      if (sarzaj.trim().isEmpty()) {
        sarzaj = "[]";
        Files.writeString(datoteka, sarzaj, StandardCharsets.UTF_8);
      }
      var partneriNiz = gson.fromJson(br, Partner[].class);
      var partneriTok = Stream.of(partneriNiz);
      partneriTok.forEach(p -> this.partneri.put(p.id(), p));
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  /**
   * Metoda koja služi za učitavanje karte pića iz pripadajućih datoteka na početku rada programa.
   *
   * @return vraća true ako je uspiješno učitavanje
   */
  public boolean ucitajKartuPica() {
    var nazivDatoteke = this.konfig.dajPostavku("datotekaKartaPica");
    if (nazivDatoteke == null) {
      return false;
    }
    var datoteka = Path.of(nazivDatoteke);
    if (!Files.exists(datoteka) || !Files.isRegularFile(datoteka) || !Files.isReadable(datoteka)) {
      return false;
    }
    try (var br = Files.newBufferedReader(datoteka)) {
      Gson gson = new Gson();
      var kartaPicaNiz = gson.fromJson(br, KartaPica[].class);
      var kartaPicaTok = Stream.of(kartaPicaNiz);
      kartaPicaTok.forEach(kp -> this.kartaPica.put(kp.id(), kp));
    } catch (IOException ex) {
      return false;
    }

    return true;
  }

  /**
   * Metoda koja služi za učitavanje kuhinja iz pripadajućih datoteka na početku rada programa.
   *
   * @return vraća true ako je uspiješno učitavanje
   */
  public boolean ucitajKuhinje() {
    for (Object postavka : konfig.dajSvePostavke().keySet()) {
      String kljuc = postavka.toString();
      if (predlozakUcitavanjeKuhinje.matcher(kljuc).matches()) {
        String vrijednost = konfig.dajPostavku(kljuc);
        Matcher matcherVrijednost = predlozakVrijednostiKuhinje.matcher(vrijednost);

        if (matcherVrijednost.matches()) {
          String oznaka = matcherVrijednost.group("oznaka");
          String naziv = matcherVrijednost.group("naziv");

          Path putanjaDatoteke = Path.of(kljuc + ".json");
          if (Files.exists(putanjaDatoteke) && Files.isRegularFile(putanjaDatoteke)
              && Files.isReadable(putanjaDatoteke)) {
            kuhinje.put(oznaka, naziv);
            if (!ucitajJelovnik(putanjaDatoteke, oznaka)) {
              return false;
            }
          }
        }
      }
    }
    if (kuhinje.isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * Metoda koja služi za učitavanje jelovnika pojedine kuhinje koja je učitana.
   *
   * @param putanjaDatoteke datoteka u kojoj se nalazi jelovnik
   * @param oznaka oznaka vrste kuhinje
   * @return vraća true ako je uspiješno učitavanje
   */
  public Boolean ucitajJelovnik(Path putanjaDatoteke, String oznaka) {
    try (var br = Files.newBufferedReader(putanjaDatoteke)) {
      Gson gson = new Gson();
      var jelovnikNiz = gson.fromJson(br, Jelovnik[].class);
      var jelovnikTok = Stream.of(jelovnikNiz);
      Map<String, Jelovnik> jelovnikRijecnik = new HashMap<>();
      jelovnikTok.forEach(j -> jelovnikRijecnik.put(j.id(), j));
      jelovnici.put(oznaka, jelovnikRijecnik);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Metoda koja služi za spremanje obračuna iz kolekcije u pripadajuću datoteku.
   *
   * @param gson instanca {@code com.google.gson.Gson} koja se koristi za pretvaranje Java objekata
   *        u JSON i obrnuto
   * @param noviObracuni popis novih obračuna
   * @return vraća true ako je sve uspiješno spremljeno
   * @throws IOException Signal koji ja javlja da se desila iznimka tipa { @code IOException }
   */
  public Boolean spremiObracun(Gson gson, Obracun[] noviObracuni) throws IOException {
    String nazivDatoteke = konfig.dajPostavku("datotekaObracuna");
    if (nazivDatoteke == null)
      return false;
    Path putanjaObracuna = Path.of(nazivDatoteke);
    List<Obracun> listaObracuna = new ArrayList<>();
    if (Files.exists(putanjaObracuna) && Files.isRegularFile(putanjaObracuna)
        && Files.isReadable(putanjaObracuna)) {
      try (BufferedReader br = Files.newBufferedReader(putanjaObracuna, StandardCharsets.UTF_8)) {
        Obracun[] prethodniObracuni = gson.fromJson(br, Obracun[].class);
        if (prethodniObracuni != null) {
          listaObracuna.addAll(Arrays.asList(prethodniObracuni));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Files.deleteIfExists(putanjaObracuna);
    }

    listaObracuna.addAll(Arrays.asList(noviObracuni));
    try (Writer writer = Files.newBufferedWriter(putanjaObracuna, StandardCharsets.UTF_8)) {
      gson.toJson(listaObracuna, writer);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Metoda koja služi za spremanje partnera iz kolekcije u pripadajuću datoteku.
   *
   * @return vraća true ako je sve uspiješno spremljeno
   */
  public Boolean spremiPartnere() {
    String nazivDatoteke = konfig.dajPostavku("datotekaPartnera");
    if (nazivDatoteke == null)
      return false;
    Path putanja = Path.of(nazivDatoteke);

    try {
      if (!Files.exists(putanja)) {
        Files.createFile(putanja);
      }
      try (Writer writer = Files.newBufferedWriter(putanja, StandardCharsets.UTF_8)) {
        Gson gson = new Gson();
        gson.toJson(partneri.values(), writer);
        return true;
      }
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Kreiraj komandu kraj.
   *
   * @return the string
   */
  private String kreirajKomanduKraj() {
    var kodZaKraj = this.kodZaKraj;

    StringBuilder komanda = new StringBuilder();
    komanda.append("KRAJ ").append(kodZaKraj).append("\n");
    return komanda.toString();
  }

  /**
   * Metoda koja služi za učitavanje konfiguracijske datoteke.
   *
   * @param nazivDatoteke naziv konfiguracijske datoteke
   * @return vraća true, ako je uspješno učitavanje konfiguracije
   */
  public boolean ucitajKonfiguraciju(String nazivDatoteke) {
    try {
      this.konfig = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
      return true;
    } catch (NeispravnaKonfiguracija ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
    }
    return false;
  }
}
