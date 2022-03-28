FROM tomcat:9.0-alpine

LABEL maintainer='Tjidde.Nieuwenhuizen@merkator.com'

ADD /target/KARgeo.war /usr/local/tomcat/webapps

EXPOSE 8080

CMD [ "catalina.sh", "run" ]