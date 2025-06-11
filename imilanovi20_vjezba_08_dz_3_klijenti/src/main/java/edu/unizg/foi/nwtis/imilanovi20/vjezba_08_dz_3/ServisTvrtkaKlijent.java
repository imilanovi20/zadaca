package edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "klijentTvrtka")
@Path("api/tvrtka")
public interface ServisTvrtkaKlijent {
  @HEAD
  public Response headPosluzitelj();

  @Path("status/{id}")
  @HEAD
  public Response headPosluziteljStatus(@PathParam("id") int id);

  @Path("pauza/{id}")
  @HEAD
  public Response headPosluziteljPauza(@PathParam("id") int id);

  @Path("start/{id}")
  @HEAD
  public Response headPosluziteljStart(@PathParam("id") int id);

  @Path("kraj")
  @HEAD
  public Response headPosluziteljKraj();

  @Path("kraj/info")
  @HEAD
  public Response headPosluziteljKrajInfo();

  @Path("partner")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartneri();

  @Path("partner/{id}")
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  public Response getPartner(@PathParam("id") int id);

  // TODO na isti način definirati za ostale operacije RESTful servisa
}
