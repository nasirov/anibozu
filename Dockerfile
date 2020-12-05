FROM openjdk:8-jdk-alpine
ENV JAVA_TOOL_OPTIONS="-Xms300m -Xmx300m -XX:+PrintCommandLineFlags -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
                       -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -verbose:gc -XX:+PrintGCDetails" \
    PORT=${PORT} \
    FTS_BASIC_AUTH=${FTS_BASIC_AUTH} \
    MS_BASIC_AUTH=${MS_BASIC_AUTH}
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]