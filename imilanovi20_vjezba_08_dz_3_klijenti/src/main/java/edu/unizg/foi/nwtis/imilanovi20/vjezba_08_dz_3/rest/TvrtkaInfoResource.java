package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.rest;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.ws.WebSocketTvrtka;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("nwtis/v1/api/tvrtka")
public class TvrtkaInfoResource {

  @Path("kraj/info")
  @GET
  @Operation(summary = "Informacija o zaustavljanju poslužitelja tvrtka")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response getPosluziteljKrajInfo() {
    WebSocketTvrtka.send("KRAJ");
    return Response.status(Response.Status.OK).build();
  }

  @Path("obracun/ws")
  @GET
  @Operation(summary = "Informacija o novom obračunu")
  @APIResponses(value = {@APIResponse(responseCode = "200", description = "Uspješna operacija"),
      @APIResponse(responseCode = "409", description = "Pogrešna operacija")})
  @Counted(name = "brojZahtjeva_headPosluziteljKrajInfo",
      description = "Koliko puta je pozvana operacija servisa")
  @Timed(name = "trajanjeMetode_headPosluziteljKrajInfo", description = "Vrijeme trajanja metode")
  public Response getObracunWS() {
    WebSocketTvrtka.send("OBRAČUN WS");
    return Response.status(Response.Status.OK).build();
  }

}
