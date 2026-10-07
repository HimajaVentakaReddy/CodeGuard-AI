FROM maven:3.9.11-eclipse-temurin-17 AS build

WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
	&& apt-get install --no-install-recommends -y curl \
	&& rm -rf /var/lib/apt/lists/* \
	&& groupadd --system --gid 10001 app \
	&& useradd --system --uid 10001 --gid app --no-create-home app

WORKDIR /app
COPY --from=build /build/target/codeguard-ai-0.0.1-SNAPSHOT.jar /app/app.jar

USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
