FROM ubuntu:latest

COPY ./target/maven-jlink/default/ /usr/app/


WORKDIR /usr/app/podaci

EXPOSE 8000
EXPOSE 8001
EXPOSE 8002

CMD ["bin/java", "-m", "imilanovi20_vjezba_07_dz_2_tvrtka/edu.unizg.foi.nwtis.imilanovi20.vjezba_07_dz_2.PosluziteljTvrtka", "NWTiS_04_tvrtka.txt"]
