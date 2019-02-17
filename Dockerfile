FROM openjdk:8-jdk-alpine
ENV JAVA_TOOL_OPTIONS -XX:+UseG1GC -Xms1g -Xmx1g -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom
VOLUME /tmp
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]