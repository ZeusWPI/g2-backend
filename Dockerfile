FROM openjdk:8-alpine

COPY target/uberjar/g2.jar /g2/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/g2/app.jar"]
