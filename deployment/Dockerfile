# syntax=docker/dockerfile:1.7

########## STAGE 1: BUILD ##########
FROM gradle:8.10.2-jdk17-jammy AS build
WORKDIR /workspace
COPY . .

# Si tu wrapper viene de Windows y te da permiso/CRLF, descomenta:
# RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/* \
#  && dos2unix gradlew && chmod +x gradlew

# 1) Descubre buildDir real de :app-service
# 2) Empaqueta bootJar
# 3) Copia jar final (excluye *-plain.jar) a /workspace/app.jar
RUN BUILD_DIR="$(gradle :app-service:properties -q --no-daemon | awk -F': ' '/^buildDir: /{print $2}')" \
 && echo "buildDir=: ${BUILD_DIR}" \
 && gradle :app-service:bootJar -x test --no-daemon --stacktrace \
 && ls -l "${BUILD_DIR}/libs" \
 && APP_JAR="$(ls -1 "${BUILD_DIR}"/libs/*.jar | grep -v 'plain' | head -n1)" \
 && echo "Usando bootJar -> ${APP_JAR}" \
 && test -n "${APP_JAR}" \
 && cp "${APP_JAR}" /workspace/app.jar

########## STAGE 2: RUNTIME ##########
# Si quieres coherencia con AUTENTICACION (OpenJ9 por -Xshareclasses), usa Semeru:
FROM ibm-semeru-runtimes:open-17-jre
# (Si no usas -Xshareclasses puedes usar eclipse-temurin:17-jre-alpine)

WORKDIR /app
COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
