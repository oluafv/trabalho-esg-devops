# ==========================================
# Estagio 1: Build da aplicacao (Builder)
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copiar arquivos de definicao e wrapper para cache inteligente de dependencias
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download de dependencias em camada separada (cache layer)
RUN ./mvnw dependency:go-offline -B

# Copiar codigo-fonte e gerar o pacote executavel JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests=true

# ==========================================
# Estagio 2: Imagem de Execucao (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

# Criacao de usuario nao-root (boas praticas DevSecOps)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copiar apenas o artefato final gerado pelo builder
COPY --from=builder /build/target/*.jar app.jar

# Ajuste de permissoes de seguranca
RUN chown -R appuser:appgroup /app

USER appuser

# Porta padrao do Spring Boot
EXPOSE 8080

# Otimizacao JVM para ambientes conteinerizados
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]