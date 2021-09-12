FROM openjdk:8-jdk-alpine
ENV JAVA_TOOL_OPTIONS="-Xms300m -Xmx300m -XX:+PrintCommandLineFlags -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
                       -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -verbose:gc -XX:+PrintGCDetails" \
    PORT=${PORT} \
    FTS_BASIC_AUTH=${FTS_BASIC_AUTH} \
    MS_BASIC_AUTH=${MS_BASIC_AUTH} \
    SS_BASIC_AUTH=${SS_BASIC_AUTH} \
    RABBITMQ_HOST=${RABBITMQ_HOST} \
    RABBITMQ_USERNAME=${RABBITMQ_USERNAME} \
    RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD} \
    RABBITMQ_HOST_2=${RABBITMQ_HOST_2} \
    RABBITMQ_USERNAME_2=${RABBITMQ_USERNAME_2} \
    RABBITMQ_PASSWORD_2=${RABBITMQ_PASSWORD_2}
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD ["java","-jar","/app.jar"]