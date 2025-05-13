# Etapa 1: build (usando Maven)
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia o pom.xml e resolve dependências separadamente
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia todo o código-fonte e gera o .jar
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: imagem final enxuta
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copia apenas o JAR gerado da etapa anterior
COPY --from=builder /app/target/credit-simulator-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]