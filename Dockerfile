FROM eclipse-temurin:22-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

EXPOSE 10000

CMD ["java", "-jar", "target/music-server-0.0.1-SNAPSHOT.jar"]
