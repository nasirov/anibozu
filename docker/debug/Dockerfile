FROM openjdk:8-jdk-alpine
ADD anime-checker-1.0.jar app.jar
ENV PORT ${PORT}
RUN wget https://download-keycdn.ej-technologies.com/jprofiler/jprofiler_linux_10_1_5.tar.gz -P /tmp/ && \
  tar -xzf /tmp/jprofiler_linux_10_1_5.tar.gz -C /usr/local &&\
  rm /tmp/jprofiler_linux_10_1_5.tar.gz
CMD ["java","-jar","/app.jar"]