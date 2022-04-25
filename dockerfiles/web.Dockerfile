FROM openjdk:12-alpine AS builder
#Get APK up to date
RUN apk update && apk upgrade
VOLUME /root/.m2
#Install Maven
RUN apk add maven

#Git
RUN apk add git
RUN mkdir /kar
RUN git clone https://github.com/tjidde-nl/kargeotool.git /kar

RUN mvn -f /kar clean install

#Create TOMCAT
FROM tomcat:9.0-alpine

LABEL maintainer='Tjidde.Nieuwenhuizen@merkator.com'
COPY --from=builder $HOME/kar/target/* /usr/local/tomcat/webapps/
ADD ../EXT_Files/jar/* lib/
ADD ../EXT_Files/war/* /usr/local/tomcat/webapps/
ADD ../EXT_Files/context/* conf/
EXPOSE 8080

CMD [ "catalina.sh", "run" ]