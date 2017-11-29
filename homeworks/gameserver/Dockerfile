FROM openjdk:8-jre-alpine
MAINTAINER comanda
COPY build/libs/gameserver-0.0.1-SNAPSHOT.jar /services/gameserver.jar
#EXPOSE will not open port to outer world, just inside cloud
#Billing service port
EXPOSE 8090
CMD ["java", "-jar", "/services/matchmaker.jar"]