FROM azul/zulu-openjdk-alpine:11.0.12
ENV PORT=${PORT} \
    FTS_BASIC_AUTH=${FTS_BASIC_AUTH} \
    MS_BASIC_AUTH=${MS_BASIC_AUTH} \
    SS_BASIC_AUTH=${SS_BASIC_AUTH} \
    RABBITMQ_HOST=${RABBITMQ_HOST} \
    RABBITMQ_USERNAME=${RABBITMQ_USERNAME} \
    RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD} \
    RABBITMQ_HOST_2=${RABBITMQ_HOST_2} \
    RABBITMQ_USERNAME_2=${RABBITMQ_USERNAME_2} \
    RABBITMQ_PASSWORD_2=${RABBITMQ_PASSWORD_2} \
    JVM_ARGS="-XX:+UnlockExperimentalVMOptions -XshowSettings -server -XX:+PrintFlagsFinal -XX:+PrintCommandLineFlags \
    -Xms300m -Xmx300m -XX:+AlwaysActAsServerClassMachine \
    -Dlog4j2.formatMsgNoLookups=true -XX:+ExitOnOutOfMemoryError \
    -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD java -jar ${JVM_ARGS} /app.jar