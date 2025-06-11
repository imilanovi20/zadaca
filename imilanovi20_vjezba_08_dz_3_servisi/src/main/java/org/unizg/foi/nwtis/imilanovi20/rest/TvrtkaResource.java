package org.unizg.foi.nwtis.imilanovi20.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import com.google.gson.Gson;
import edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.dao.ObracunDAO;
import edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.dao.PartnerDAO;
import edu.unizg.foi.nwtis.podaci.Jelovnik;
import edu.unizg.foi.nwtis.podaci.KartaPica;
import edu.unizg.foi.nwtis.podaci.Obracun;
import edu.unizg.foi.nwtis.podaci.Partner;
import edu.unizg.foi.nwtis.podaci.PartnerPopis;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Klasa PartnerResource predstavlja REST API krajnju točku za upravljanje i nadzor poslužitelja
 * tvrtka na putanji /nwtis/v1/api/tvrtka/.
 * 
 * @author Ivan Milanović-Litre
 * @version 1.1.0
 */
@Path("api/tvrtka")
public class TvrtkaResource {

  /** Tvrtka adresa. */
  @Inject
  @ConfigProperty(name = "adresa")
  private String tvrtkaAdresa;

  /** Mrezna vrata kraj. */
  @Inject
  @ConfigProperty(name = "mreznaVrataKraj")
  private String mreznaVrataKraj;

  /** Mrezna vrata registracija. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRegistracija")
  private String mreznaVrataRegistracija;

  /** Mrezna vrata rad. */
  @Inject
  @ConfigProperty(name = "mreznaVrataRad")
  private String mreznaVrataRad;

  /** Kod za admin tvrtke. */
  @Inject
  @ConfigProperty(name = "kodZaAdminTvrtke")
  private String kodZaAdminTvrtke;

  /** Kod za kraj. */
  @Inject
  @ConfigProperty(name = "kodZaKraj")
  private String kodZaKraj;

  /** Rest konfiguracija */
  @Inject
  RestConfiguration restConfiguration;

  /**
   * Provjera statusa poslužitelja tvrtka.
   *
   * @return odgovor tipa { @code Response }
   */
  @HEAD
  @Operation(summary = "Provjera statusa poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_headPosluzitelj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluzitelj", description = "Vrijeme trajanja metode")
  public Response headPosluzitelj() {
    String komanda = kreirajKomanduProvjera();
    var statusKraj = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
    var statusRegistracija =
        posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRegistracija));
    var statusRad = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));
    if (statusKraj != null && statusRegistracija != null && statusRad != null) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Provjera statusa dijela poslužitelja tvrtka.
   *
   * @param id the id
   * @return odgovor tipa { @code Response }
   */
  @Path("status/{id}")
  @HEAD
  @Operation(summary = "Provjera statusa dijela poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_eadPosluziteljStatus",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_eadPosluziteljStatus", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStatus(@PathParam("id") int id) {
    String komanda = kreirajKomanduStatus(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK 1")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Postavljanje dijela poslužitelja tvrtka u pauzu.
   *
   * @param id the id
   * @return odgovor tipa { @code Response }
   */
  @Path("pauza/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u pauzu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljPauza",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljPauza", description = "Vrijeme trajanja metode")
  public Response headPosluziteljPauza(@PathParam("id") int id) {
    String komanda = kreirajKomanduPauza(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Postavljanje dijela poslužitelja tvrtka u rad.
   *
   * @param id the id
   * @return odgovor tipa { @code Response }
   */
  @Path("start/{id}")
  @HEAD
  @Operation(summary = "Postavljanje dijela poslužitelja tvrtka u rad")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljStart",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljStart", description = "Vrijeme trajanja metode")
  public Response headPosluziteljStart(@PathParam("id") int id) {
    String komanda = kreirajKomanduStart(id);
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Head posluzitelj kraj.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("kraj")
  @HEAD
  @Operation(summary = "Zaustavljanje poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKraj",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKraj", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKraj() {
    String komanda = kreirajKomanduKrajWS();
    var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
    if (status.equals("OK")) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Informacija o zaustavljanju poslužitelja tvrtka.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("kraj/info")
  @HEAD
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "204", description = "Bez sadržaja")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response headPosluziteljKrajInfo() {
    try {
      return Response.status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /**
   * Dohvat svih jelovnika.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("jelovnik")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih jelovnika")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnici",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnici", description = "Vrijeme trajanja metode")
  public Response getJelovnici() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);

      Map<String, List<Jelovnik>> sviJelovnici = new HashMap<>();
      for (Partner partner : partneri) {
        String komanda = kreirajKomanduJelovnik(partner.id(), partner.sigurnosniKod());
        String jelovnik =
            dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRad), "OK");
        List<Jelovnik> jelovnikPartnera = new ArrayList<>();
        if (jelovnik != null) {
          Gson gson = new Gson();
          var jelovnikNiz = gson.fromJson(jelovnik, Jelovnik[].class);
          var jelovnikTok = Stream.of(jelovnikNiz);
          jelovnikTok.forEach(j -> jelovnikPartnera.add(j));
        }
        if (!jelovnikPartnera.isEmpty())
          sviJelovnici.put(partner.vrstaKuhinje(), jelovnikPartnera);
      }
      return Response.ok(sviJelovnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat jelovnika jednog partnera.
   *
   * @param id id partnera
   * @return odgovor tipa { @code Response }
   */
  @Path("jelovnik/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jelovnika jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getJelovnik",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getJelovnik", description = "Vrijeme trajanja metode")
  public Response getJelovnik(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, false);
      if (partner == null)
        return Response.status(Response.Status.NOT_FOUND).build();
      Map<String, List<Jelovnik>> sviJelovnici = new HashMap<>();
      String komanda = kreirajKomanduJelovnik(partner.id(), partner.sigurnosniKod());
      String jelovnik = dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRad), "OK");
      List<Jelovnik> jelovnikPartnera = new ArrayList<>();
      if (jelovnik != null) {
        Gson gson = new Gson();
        var jelovnikNiz = gson.fromJson(jelovnik, Jelovnik[].class);
        var jelovnikTok = Stream.of(jelovnikNiz);
        jelovnikTok.forEach(j -> jelovnikPartnera.add(j));
      } else
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      sviJelovnici.put(partner.vrstaKuhinje(), jelovnikPartnera);
      return Response.ok(sviJelovnici).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat karte pića.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("kartapica")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat karte pića")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getKartaPica",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getKartaPica", description = "Vrijeme trajanja metode")
  public Response getKartaPica() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);
      List<KartaPica> kartaPicaPartnera = new ArrayList<>();
      for (Partner partner : partneri) {
        String komanda = kreirajKomanduKartaPica(partner.id(), partner.sigurnosniKod());
        String kartaPica =
            dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRad), "OK");
        if (kartaPica != null) {
          Gson gson = new Gson();
          var kartaPicaNiz = gson.fromJson(kartaPica, KartaPica[].class);
          var kartaPicaTok = Stream.of(kartaPicaNiz);
          kartaPicaTok.forEach(k -> kartaPicaPartnera.add(k));
          break;
        } else
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
      return Response.ok(kartaPicaPartnera).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }



  /**
   * Dohvat svih partnera.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat svih partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartneri",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartneri", description = "Vrijeme trajanja metode")
  public Response getPartneri() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = partnerDAO.dohvatiSve(false);

      return Response.ok(partneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat partnera u bazi i na poslužitelju.
   *
   * @return odgovor tipa { @code Response }
   */
  @Path("/partner/provjera")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat partnera u bazi i na poslužitelju")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartnerProvjera",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartnerProvjera", description = "Vrijeme trajanja metode")
  public Response getPartnerProvjera() {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partneri = pretvoriPartnereUPopis(partnerDAO.dohvatiSve(true));
      String komanda = kreirajKomanduPopis();
      String partenriPopis =
          dohvatiOdgovorKomande(komanda, Integer.parseInt(this.mreznaVrataRegistracija), "OK");
      List<PartnerPopis> listaPartnerPopis = new ArrayList<>();
      Gson gson = new Gson();
      var partnerPopisNiz = gson.fromJson(partenriPopis, PartnerPopis[].class);
      var partnerPopisTok = Stream.of(partnerPopisNiz);
      partnerPopisTok.forEach(k -> listaPartnerPopis.add(k));
      List<PartnerPopis> zajednickiPartneri = partneri.stream()
          .filter(p1 -> listaPartnerPopis.stream().anyMatch(p2 -> p1.id() == p2.id()))
          .collect(Collectors.toList());

      return Response.ok(zajednickiPartneri).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat jednog partnera.
   *
   * @param id id partnera
   * @return odgovor tipa { @code Response }
   */
  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat jednog partnera")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "404", description = "Ne postoji resurs"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getPartner", description = "Vrijeme trajanja metode")
  public Response getPartner(@PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var partner = partnerDAO.dohvati(id, true);
      if (partner != null) {
        return Response.ok(partner).status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodavanje partnera.
   *
   * @param partner partner
   * @return odgovor tipa { @code Response }
   */
  @Path("partner")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodavanje partnera")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "409", description = "Već postoji resurs ili druga pogreška"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postPartner",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postPartner", description = "Vrijeme trajanja metode")
  public Response postPartner(Partner partner) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var partnerDAO = new PartnerDAO(vezaBP);
      var status = partnerDAO.dodaj(partner);
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.CONFLICT).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat obračuna unutar intervala.
   *
   * @param odVr the početno vrijeme
   * @param doVr the završno vrijeme
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna unutar intervala")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObracuniOdDo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObracuniOdDo", description = "Vrijeme trajanja metode")
  public Response getObracuniOdDo(@QueryParam("od") Long odVr, @QueryParam("do") Long doVr) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      var obracuniInterval = obracunDAO.dohvatiInterval(odVr, doVr);
      return Response.ok(obracuniInterval).status(Response.Status.OK).build();

    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat obračuna jela unutar intervala.
   *
   * @param odVr the početno vrijeme
   * @param doVr the završno vrijeme
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun/jelo")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna jela unutar intervala")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObrJeloOdDo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObrJeloOdDo", description = "Vrijeme trajanja metode")
  public Response getObrJeloOdDo(@QueryParam("od") Long odVr, @QueryParam("do") Long doVr) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obrJeloInterval = obracunDAO.dohvatiJeloInterval(odVr, doVr);
      return Response.ok(obrJeloInterval).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat obračuna pića unutar intervala.
   *
   * @param odVr the početno vrijeme
   * @param doVr the završno vrijeme
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun/pice")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna pića unutar intervala")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObrPiceOdDo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObrPiceOdDo", description = "Vrijeme trajanja metode")
  public Response getObrPiceOdDo(@QueryParam("od") Long odVr, @QueryParam("do") Long doVr) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obrPiceInterval = obracunDAO.dohvatiPiceInterval(odVr, doVr);
      return Response.ok(obrPiceInterval).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dohvat obračuna partnera unutar intervala.
   *
   * @param odVr the početno vrijeme
   * @param doVr the završno vrijeme
   * @param id id partnera
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dohvat obračuna partnera unutar intervala")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_getObrPartnerOdDo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_getObrPartnerOdDo", description = "Vrijeme trajanja metode")
  public Response getObrPartnerOdDo(@QueryParam("od") Long odVr, @QueryParam("do") Long doVr,
      @PathParam("id") int id) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      var obracunDAO = new ObracunDAO(vezaBP);
      List<Obracun> obrPartnerInterval = obracunDAO.dohvatiPartnerInterval(id, odVr, doVr);
      return Response.ok(obrPartnerInterval).status(Response.Status.OK).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj obačun.
   *
   * @param obracuni obracuni
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj obačun")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracun",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracun", description = "Vrijeme trajanja metode")
  public Response postObracun(Obracun[] obracuni) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      boolean istiIdPartnera = provjerIdPartnera(obracuni);
      if (!istiIdPartnera)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      var obracunDAO = new ObracunDAO(vezaBP);
      boolean status = obracunDAO.dodaj(obracuni);
      if (status) {
        return Response.status(Response.Status.CREATED).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Dodaj obačun.
   *
   * @param obracuni obracuni
   * @return odgovor tipa { @code Response }
   */
  @Path("obracun/ws")
  @POST
  @Consumes({MediaType.APPLICATION_JSON})
  @Produces({MediaType.APPLICATION_JSON})
  @Operation(summary = "Dodaj obačun")
  @APIResponses(
      value = {@APIResponse(responseCode = "201", description = "Uspješna kreiran resurs"),
          @APIResponse(responseCode = "500", description = "Interna pogreška")})
  @Counted(name = "brojZahtjeva_postObracunWs",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_postObracunWs", description = "Vrijeme trajanja metode")
  public Response postObracunWs(Obracun[] obracuni) {
    try (var vezaBP = this.restConfiguration.dajVezu()) {
      boolean istiIdPartnera = provjerIdPartnera(obracuni);
      if (!istiIdPartnera)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      var partnerDAO = new PartnerDAO(vezaBP);
      var partnerId = obracuni[0].partner();
      var partnerObracun = partnerDAO.dohvati(partnerId, false);
      var obracunDAO = new ObracunDAO(vezaBP);
      boolean statusBaza = obracunDAO.dodaj(obracuni);
      if (statusBaza) {
        String komanda = kreirajKomanduObracunWs(obracuni, partnerObracun);
        var statusKomanda = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataRad));
        if (statusKomanda.equals("OK"))
          return Response.status(Response.Status.CREATED).build();
        else {
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
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
      var status = posaljiKomandu(komanda, Integer.parseInt(this.mreznaVrataKraj));
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
    komanda.append("STATUS ").append(this.kodZaAdminTvrtke).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu pauza.
   *
   * @param id id dijela posluzitelja
   * @return komanda
   */
  private String kreirajKomanduPauza(int id) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("PAUZA ").append(this.kodZaAdminTvrtke).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu start.
   *
   * @param id id dijela posluzitelja
   * @return komanda
   */
  private String kreirajKomanduStart(int id) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("START ").append(this.kodZaAdminTvrtke).append(" ").append(id);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu kraj.
   *
   * @return komanda
   */
  private String kreirajKomanduKrajWS() {
    StringBuilder komanda = new StringBuilder();
    komanda.append("KRAJWS ").append(this.kodZaKraj);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu jelovnik.
   *
   * @param id id partnera
   * @param sigKod sigurnosni kod
   * @return komanda
   */
  private String kreirajKomanduJelovnik(Integer id, String sigKod) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("JELOVNIK ").append(id).append(" ").append(sigKod);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu karta pica.
   *
   * @param id id partnera
   * @param sigKod sigurnosni kod
   * @return komanda
   */
  private String kreirajKomanduKartaPica(int id, String sigKod) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("KARTAPIĆA ").append(id).append(" ").append(sigKod);
    return komanda.toString();
  }

  /**
   * Kreiraj komandu popis.
   *
   * @return komanda
   */
  private String kreirajKomanduPopis() {
    StringBuilder komanda = new StringBuilder();
    komanda.append("POPIS");
    return komanda.toString();
  }

  /**
   * Pretvori partnere U popis.
   *
   * @param partneri partneri
   * @return lista partner popis
   */
  private List<PartnerPopis> pretvoriPartnereUPopis(List<Partner> partneri) {
    return partneri.stream()
        .map(partner -> new PartnerPopis(partner.id(), partner.naziv(), partner.vrstaKuhinje(),
            partner.adresa(), partner.mreznaVrata(), partner.gpsSirina(), partner.gpsDuzina()))
        .collect(Collectors.toList());
  }

  /**
   * Provjer id partnera.
   *
   * @param obracuni obracuni
   * @return true ako su svi obračuni od istog partnera
   */
  private boolean provjerIdPartnera(Obracun[] obracuni) {
    int prviId = obracuni[0].partner();
    for (Obracun o : obracuni) {
      if (o.partner() != prviId)
        return false;
    }
    return true;
  }

  /**
   * Kreiraj komandu obracun ws.
   *
   * @param obracuni obracuni
   * @param partner partner
   * @return komanda
   */
  private String kreirajKomanduObracunWs(Obracun[] obracuni, Partner partner) {
    StringBuilder komanda = new StringBuilder();
    komanda.append("OBRAČUNWS ").append(partner.id()).append(" ").append(partner.sigurnosniKod())
        .append("\n");
    Gson gson = new Gson();
    String jsonObracuni = gson.toJson(obracuni);
    komanda.append(jsonObracuni);
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
    komanda.append("SPAVA ").append(this.kodZaAdminTvrtke).append(" ").append(vrijeme);
    return komanda.toString();
  }

  /**
   * Posalji komandu.
   *
   * @param komanda komanda
   * @param mreznaVrataSlanje mrezna vrata slanje
   * @return odgovor
   */
  private String posaljiKomandu(String komanda, Integer mreznaVrataSlanje) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, mreznaVrataSlanje);
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
   * @return odgovor
   */
  private String dohvatiOdgovorKomande(String komanda, Integer mreznaVrataSlanje,
      String ocekivaniOdg) {
    try {
      var mreznaUticnica = new Socket(this.tvrtkaAdresa, mreznaVrataSlanje);
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
