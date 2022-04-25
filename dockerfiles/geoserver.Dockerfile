FROM tomcat:9.0-alpine

LABEL maintainer='Tjidde.Nieuwenhuizen@merkator.com'

ENV GEOSERVER_VERSION=2.20.4
# RUN wget https://sourceforge.net/projects/geoserver/files/GeoServer/$GEOSERVER_VERSION/geoserver-$GEOSERVER_VERSION-war.zip
COPY ../EXT_Files/geoserver/geoserver-$GEOSERVER_VERSION-war.zip /
RUN unzip -qo /geoserver-$GEOSERVER_VERSION-war.zip -d /

ENV GEOSERVER_DIR /usr/local/tomcat/webapps/geoserver
RUN mkdir -p $GEOSERVER_DIR
RUN unzip -qo /geoserver.war -d $GEOSERVER_DIR

COPY ../EXT_Files/geoserver/workspaces/ /geoserver-workspaces
ENV GEOSERVER_DATA_DIR $GEOSERVER_DIR/data
RUN cp -r /geoserver-workspaces/* $GEOSERVER_DATA_DIR/workspaces/

EXPOSE 8080

CMD [ "catalina.sh", "run" ]
