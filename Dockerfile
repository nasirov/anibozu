FROM openjdk:8-jdk-alpine
ENV JAVA_TOOL_OPTIONS -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom
ENV PORT ${PORT}
ENV GITHUB_ACCESS_TOKEN ${GITHUB_ACCESS_TOKEN}
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]