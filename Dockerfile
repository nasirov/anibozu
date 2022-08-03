FROM azul/zulu-openjdk-alpine:11.0.12
ENV PORT=${PORT} \
    FTS_BASIC_AUTH=${FTS_BASIC_AUTH} \
    MS_BASIC_AUTH=${MS_BASIC_AUTH} \
    MALLOC_ARENA_MAX=2 \
    JVM_ARGS="-XX:+UnlockExperimentalVMOptions -XshowSettings:vm -server -XX:+PrintFlagsFinal -XX:+PrintCommandLineFlags \
    -XX:+UseContainerSupport -Xms230m -Xmx230m -Xss512k -XX:MetaspaceSize=100M -XX:MaxMetaspaceSize=100M -XX:MaxDirectMemorySize=150m \
    -XX:InitialCodeCacheSize=25M -XX:ReservedCodeCacheSize=25M -XX:+AlwaysActAsServerClassMachine \
    -Dlog4j2.formatMsgNoLookups=true -XX:+ExitOnOutOfMemoryError \
    -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom"
ARG JAR_FILE
ADD ${JAR_FILE} app.jar
CMD java -jar ${JVM_ARGS} /app.jar