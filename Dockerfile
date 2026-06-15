FROM eclipse-temurin:21

ENV PORT 8080
ENV CLASSPATH /opt/lib
EXPOSE 8080
RUN apt-get update && apt-get install -y unzip locales locales-all tzdata
ENV TZ=Europe/Madrid
RUN ln -fs /usr/share/zoneinfo/Europe/Madrid /etc/localtime && dpkg-reconfigure -f noninteractive tzdata

# Configurar locales
RUN locale-gen es_ES.UTF-8 && update-locale LANG=es_ES.UTF-8

# Copy jar file
COPY ./rowbot2-data-importer-boot/target/rowbot2-data-importer-boot-*.jar /opt/rowbot2-data-importer-boot.jar
WORKDIR /opt
CMD ["/bin/bash", "-c", "case $ENVIRONMENT_PROFILE in 'production') java $JVM_OPTIONS -jar rowbot2-data-importer-boot.jar --spring.profiles.active=production;; *) java $JVM_OPTIONS -jar rowbot2-data-importer-boot.jar --spring.profiles.active=staging;; esac;"]
