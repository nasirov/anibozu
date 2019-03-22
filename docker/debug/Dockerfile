FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD anime-checker-2.0.jar app.jar
RUN wget https://download-keycdn.ej-technologies.com/jprofiler/jprofiler_linux_10_1_5.tar.gz -P /tmp/ && \
  tar -xzf /tmp/jprofiler_linux_10_1_5.tar.gz -C /usr/local &&\
  rm /tmp/jprofiler_linux_10_1_5.tar.gz
EXPOSE 8849
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]