FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/

WORKDIR /usr/app/podaci

EXPOSE 8010
EXPOSE 8011

CMD ["/usr/app/bin/java", "-m", "imilanovi20_vjezba_08_dz_3_partner/edu.unizg.foi.nwtis.imilanovi20.vjezba_08_dz_3.PosluziteljPartner", "NWTiS_04_partner_1.txt", "PARTNER"]