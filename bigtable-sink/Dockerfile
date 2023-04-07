FROM eclipse-temurin:17
RUN mkdir /opt/app
COPY build/libs/bigtable-sink-all.jar /opt/app
RUN mkdir -p /root/.config/gcloud
COPY application_default_credentials.json /root/.config/gcloud
CMD ["java", "-jar", "/opt/app/bigtable-sink-all.jar"]