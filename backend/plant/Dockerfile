FROM eclipse-temurin:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} plant.jar
ENTRYPOINT ["java","-jar","/plant.jar"]
EXPOSE 8080