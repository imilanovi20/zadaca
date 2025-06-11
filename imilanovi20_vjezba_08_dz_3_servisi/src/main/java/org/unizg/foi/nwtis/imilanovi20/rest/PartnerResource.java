package org.unizg.foi.nwtis.imilanovi20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.dao.KorisnikDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Korisnik;
import edu.unizg.foi.nwtis.podaci.Narudzba;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa PartnerResource predstavlja REST API krajnju točku za upravljanje i nadzor poslužitelja
 * partnera na putanji /nwtis/v1/api/partner/.
 * 
 * @author Ivan Milanović-Litre
 * @version 1.1.0
 */
@Path("api/partner")
public class PartnerResource {

  /** Partner adresa. */
  @Inject
  @ConfigProperty(name = "adresaPartner")
  private String partnerAdresa;

  /** Mrezna vrata kraj partner. */
  @Inject
  @ConfigProperty(name = "mreznaVrataKrajPartner")
  private String mreznaVrataKrajPartner;

  /** Mrezna vrata rad partner. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRadPartner")
  private String mreznaVrataRadPartner;

  /** Kod za admin partnera. */
  @Inject
  @ConfigProperty(name = "kodZaAdminPartnera")
  private String kodZaAdminPartnera;



  /** Kod za kraj. */
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  /** Rest konfiguracija */
  @Inject
  RestConfiguration restConfiguration;

  /**
   * Provjera statusa poslužitelja partner.
   *
   * @return odgovor tipa { @code Response }
   */
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_headPosluzitelj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    String komanda = kreirajKomanduProvjera();
    var statusKraj = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
    var statusRad = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
    if (statusKraj != null && statusRad != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Provjera statusa dijela poslužitelja partner.
   *
   * @param id id dijela posluzitelja
   * @return odgovor tipa { @code Response }
   */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    String komanda = kreirajKomanduStatus(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
    if (status.equals("OK 1")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Postavljanje dijela poslužitelja partnera u pauzu.
   *
   * @param id id dijela posluzitelja
   * @return odgovor tipa { @code Response }
   */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partnera u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    String komanda = kreirajKomanduPauza(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Postavljanje dijela poslužitelja partnera u rad.
   *
   * @param id id dijela posluzitelja
   * @return odgovor tipa { @code Response }
   */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja partnera u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
    String komanda = kreirajKomanduStart(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Zaustavljanje poslužitelja partner.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja partner")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKraj() {
    String komanda = kreirajKomanduKraj();
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Dohvat jelovnika partnera.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduJelovnik(korisnik);
        String jelovniciJson =
            dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRadPartner), "OK");
        if (jelovniciJson != null) {
          Gson gson = new Gson();
          var jelovnikNiz = gson.fromJson(jelovniciJson, Jelovnik[].class);
          return Response.ok(jelovnikNiz).status(Response.Status.OK).build();
        } else
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat karte pića partnera.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pića partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
  public Response getKartaPica(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduKartaPica(korisnik);
        String kartaPicaJson =
            dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRadPartner), "OK");
        if (kartaPicaJson != null) {
          Gson gson = new Gson();
          var kartaPicaNiz = gson.fromJson(kartaPicaJson, KartaPica[].class);
          return Response.ok(kartaPicaNiz).status(Response.Status.OK).build();
        } else
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat otvorene narudžbe partnera.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("narudzba")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat otvorene narudžbe partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getNarudzbe",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getNarudzbe", description = "Vrijeme trajanja metode")
  public Response getNarudzbe(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduStanje(korisnik);
        String narudzbeJson =
            dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRadPartner), "OK");
        if (narudzbeJson != null) {
          Gson gson = new Gson();
          var narudzbaNiz = gson.fromJson(narudzbeJson, Narudzba[].class);
          return Response.ok(narudzbaNiz).status(Response.Status.OK).build();
        } else
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj narudzbu.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("narudzba")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj narudzbu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postNarudzba",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postNarudzba", description = "Vrijeme trajanja metode")
  public Response postNarudzba(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduNarudzba(korisnik);
        var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
        if (status.equals("OK"))
          return Response.status(Response.Status.CREATED).build();
        else
          return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj jelo.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @param narudzba narudzba
   * @return odgovor tipa { @code Response }
   */
  @Path("jelo")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj jelo")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postJelo", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postJelo", description = "Vrijeme trajanja metode")
  public Response postJelo(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka, Narudzba narudzba) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduJelo(dohvaceniKorisnik, narudzba);
        var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
        if (status.equals("OK"))
          return Response.status(Response.Status.CREATED).build();
        else
          return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj piće.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @param narudzba narudzba
   * @return odgovor tipa { @code Response }
   */
  @Path("pice")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj pice")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPice", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPice", description = "Vrijeme trajanja metode")
  public Response postPice(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka, Narudzba narudzba) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduPice(dohvaceniKorisnik, narudzba);
        var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
        if (status.equals("OK"))
          return Response.status(Response.Status.CREATED).build();
        else
          return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj račun.
   *
   * @param korisnik korisnik
   * @param lozinka lozinka korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("racun")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj račun")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "401", description = "Korisnik nije prijavljen"),
      @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postRacun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postRacun", description = "Vrijeme trajanja metode")
  public Response postRacun(@HeaderParam("korisnik") String korisnik,
      @HeaderParam("lozinka") String lozinka) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnik, lozinka, true);
      if (dohvaceniKorisnik != null) {
        var komanda = kreirajKomanduRacun(dohvaceniKorisnik);
        var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRadPartner));
        if (status.equals("OK"))
          return Response.status(Response.Status.CREATED).build();
        else
          return Response.status(Response.Status.CONFLICT).build();
      }
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat svih korisnika.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("korisnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnici",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnici", description = "Vrijeme trajanja metode")
  public Response getKorisnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnici = korisnikDAO.dohvatiSve();
      return Response.ok(dohvaceniKorisnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat korisnika s id-om.
   *
   * @param korisnikId id korisnika
   * @return odgovor tipa { @code Response }
   */
  @Path("korisnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat korisnika s id-om")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKorisnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKorisnik", description = "Vrijeme trajanja metode")
  public Response getKorisnik(@PathParam("id") String korisnikId) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var dohvaceniKorisnik = korisnikDAO.dohvati(korisnikId, korisnikId, false);
      if (dohvaceniKorisnik != null) {
        return Response.ok(dohvaceniKorisnik).status(Response.Status.OK).build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj korisnika.
   *
   * @param korisnik korisnik
   * @return odgovor tipa { @code Response }
   */
  @Path("korisnik")
  @POST
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj korisnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postRacun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postRacun", description = "Vrijeme trajanja metode")
  public Response postKorisnik(Korisnik korisnik) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var korisnikDAO = new KorisnikDAO(vezaBP);
      var status = korisnikDAO.dodaj(korisnik);
      if (status)
        return Response.status(Response.Status.CREATED).build();
      else
        return Response.status(Response.Status.CONFLICT).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat spavanja.
   *
   * @param vrijeme vrijeme
   * @return odgovor tipa { @code Response }
   */
  @Path("spava")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat spavanja")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getSpava", description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getSpava", description = "Vrijeme trajanja metode")
  public Response getSpava(@QueryParam("vrijeme") Long vrijeme) {
    if (vrijeme != null) {
      String komanda = kreirajKomanduSpava(vrijeme);
      var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKrajPartner));
      if (status.equals("OK")) {
        return Response.status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Kreiraj komandu provjera.
   *
   * @return komanda
   */
  private String kreirajKomanduProvjera() {
    StringBuilder komanda = new StringBuilder();
    komanda.append("PROVJERA");
    return komanda.toString();
  }

  /**
   * Kreiraj komandu status.
   *
   * @param id id dijela poslužitelja
   * @return komanda
   */
  private String kreirajKomanduStatus(int id) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("STATUS ").append(this.kodZaAdminPartnera).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu pauza.
   *
   * @param id id dijela poslužitelja
   * @return komanda
   */
  private String kreirajKomanduPauza(int id) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("PAUZA ").append(this.kodZaAdminPartnera).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu start.
   *
   * @param id id dijela poslužitelja
   * @return komanda
   */
  private String kreirajKomanduStart(int id) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("START ").append(this.kodZaAdminPartnera).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu kraj.
   *
   * @return komanda
   */
  private String kreirajKomanduKraj() {
    StringBuilder komanda = new StringBuilder();
    komanda.append("KRAJ ").append(this.kodZaKraj);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu jelovnik.
   *
   * @param korisnik korisnik
   * @return komanda
   */
  private String kreirajKomanduJelovnik(String korisnik) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("JELOVNIK ").append(korisnik);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu karta pica.
   *
   * @param korisnik korisnik
   * @return komanda
   */
  private String kreirajKomanduKartaPica(String korisnik) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("KARTAPIĆA ").append(korisnik);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu stanje.
   *
   * @param korisnik korisnik
   * @return komanda
   */
  private String kreirajKomanduStanje(String korisnik) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("STANJE ").append(korisnik);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu narudzba.
   *
   * @param korisnik korisnik
   * @return komanda
   */
  private String kreirajKomanduNarudzba(String korisnik) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("NARUDŽBA ").append(korisnik);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu jelo.
   *
   * @param dohvaceniKorisnik dohvaceni korisnik
   * @param narudzba narudzba
   * @return komanda
   */
  private String kreirajKomanduJelo(Korisnik dohvaceniKorisnik, Narudzba narudzba) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("JELO ").append(dohvaceniKorisnik.korisnik()).append(" ").append(narudzba.id())
        .append(" ").append(narudzba.kolicina());
    return komanda.toString();
  }

  /**
   * Kreiraj komandu pice.
   *
   * @param dohvaceniKorisnik dohvaceni korisnik
   * @param narudzba narudzba
   * @return komanda
   */
  private String kreirajKomanduPice(Korisnik dohvaceniKorisnik, Narudzba narudzba) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("PIĆE ").append(dohvaceniKorisnik.korisnik()).append(" ").append(narudzba.id())
        .append(" ").append(narudzba.kolicina());
    return komanda.toString();
  }

  /**
   * Kreiraj komandu racun.
   *
   * @param dohvaceniKorisnik dohvaceni korisnik
   * @return komanda
   */
  private String kreirajKomanduRacun(Korisnik dohvaceniKorisnik) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("RAČUN ").append(dohvaceniKorisnik.korisnik());
    return komanda.toString();
  }

  /**
   * Kreiraj komandu spava.
   *
   * @param vrijeme vrijeme
   * @return komanda
   */
  private String kreirajKomanduSpava(Long vrijeme) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("SPAVA ").append(this.kodZaAdminPartnera).append(" ").append(vrijeme);
    return komanda.toString();
  }

  /**
   * Posalji komandu.
   *
   * @param komanda komanda
   * @param mreznaVrataSlanje mrezna vrata slanje
   * @return odgovor komande
   */
  private String posaljiKomandu(String komanda, Integer mreznaVrataSlanje) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, mreznaVrataSlanje);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var linija = in.readLine();
      mreznaUticnica.shutdownInput();
      mreznaUticnica.close();
      return linija;
    } catch (IOException e) {
    }
    return null;
  }

  /**
   * Dohvati odgovor komande.
   *
   * @param komanda komanda
   * @param mreznaVrataSlanje mrezna vrata slanje
   * @param ocekivaniOdg ocekivani odg
   * @return odgovor komande
   */
  private String dohvatiOdgovorKomande(String komanda, Integer mreznaVrataSlanje,
      String ocekivaniOdg) {
    try {
      var mreznaUticnica = new Socket(this.partnerAdresa, mreznaVrataSlanje);
      BufferedReader in =
          new BufferedReader(new InputStreamReader(mreznaUticnica.getInputStream(), "utf8"));
      PrintWriter out =
          new PrintWriter(new OutputStreamWriter(mreznaUticnica.getOutputStream(), "utf8"));
      out.write(komanda + "\n");
      out.flush();
      mreznaUticnica.shutdownOutput();
      var odgovor = in.readLine();
      mreznaUticnica.shutdownInput();
      if (!odgovor.equals(ocekivaniOdg)) {
        return null;
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
      mreznaUticnica.close();
      return jsonOdgovor.toString();
    } catch (IOException e) {
    }
    return null;
  }

}
