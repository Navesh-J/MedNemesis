# =========================================================
# BUILD STAGE
# =========================================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests


# =========================================================
# RUNTIME STAGE
# =========================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# ---------------------------------------------------------
# Install native libraries required by Tess4J / Tesseract
# ---------------------------------------------------------

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        tesseract-ocr \
        tesseract-ocr-eng \
        libleptonica-dev \
    && rm -rf /var/lib/apt/lists/*

# ---------------------------------------------------------
# Application JAR
# ---------------------------------------------------------

COPY --from=build /app/target/*.jar app.jar

# ---------------------------------------------------------
# Port
# ---------------------------------------------------------

EXPOSE 8080

# ---------------------------------------------------------
# Start application
# ---------------------------------------------------------

ENTRYPOINT ["java", "-jar", "app.jar"]