FROM openjdk:8-jdk-alpine
ENV JAVA_TOOL_OPTIONS -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom
ENV PORT ${PORT}
ENV FTS_BASIC_AUTH ${FTS_BASIC_AUTH}
ENV MS_BASIC_AUTH ${MS_BASIC_AUTH}
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]