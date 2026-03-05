FROM eclipse-temurin:25

ENV PORT 8080
ENV CLASSPATH /opt/lib
EXPOSE 8080

# Copy jar file
COPY ./rowbot2-data-importer-boot/target/rowbot2-data-importer-boot-*.jar /opt/rowbot2-data-importer-boot.jar
WORKDIR /opt
CMD ["/bin/bash", "-c", "case $ENVIRONMENT_PROFILE in 'production') java $JVM_OPTIONS -jar rowbot2-data-importer-boot.jar --spring.profiles.active=production;; *) java $JVM_OPTIONS -jar rowbot2-data-importer-boot.jar --spring.profiles.active=staging;; esac;"]
