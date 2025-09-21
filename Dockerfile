# Dockerfile multi-stage pour optimiser la taille de l'image
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Créer le répertoire de travail
WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .

# Télécharger les dépendances (layer cachable)
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Image finale optimisée
FROM eclipse-temurin:17-jre-alpine

# Métadonnées
LABEL maintainer="Boaz Housing"
LABEL description="QR Code Generator API with Boaz Housing style"
LABEL version="1.0.0"

# Installer les dépendances système nécessaires
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    wget

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Créer les répertoires nécessaires
RUN mkdir -p /app/document-qr-code-generer && \
    chown -R appuser:appgroup /app

# Définir le répertoire de travail
WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Changer les permissions
RUN chown appuser:appgroup app.jar

# Basculer vers l'utilisateur non-root
USER appuser

# Exposer le port
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SPRING_PROFILES_ACTIVE=prod \
    APP_QR_OUTPUT_DIRECTORY=/app/document-qr-code-generer \
    APP_QR_BASE_URL=https://housing.boaz-study.tech

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Point d'entrée optimisé
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]