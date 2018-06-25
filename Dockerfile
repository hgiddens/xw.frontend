FROM cloudscala/scala-graalvm:scala-2.12.6-sbt-1.1.5-graalvm-1.0.0-rc1 AS build

LABEL maintainer "hgiddens@gmail.com"

COPY . /src

WORKDIR /src

RUN sbt -batch server/assembly

FROM debian:stable-slim AS run

LABEL maintainer "hgiddens@gmail.com"

EXPOSE 8080

WORKDIR /app

ENV JAVA_HOME /opt/graalvm-1.0.0-rc1-jre
ENV PATH $JAVA_HOME/bin:$PATH

COPY --from=build /src/server/target/scala-2.12/frontend.jar /app/frontend.jar
COPY --from=build /root/graalvm-1.0.0-rc1/jre $JAVA_HOME

USER nobody

CMD ["java", \
     "-XX:+UseG1GC", \
     "-XX:+UnlockExperimentalVMOptions", \
     "-XX:+UseCGroupMemoryLimitForHeap", \
     "-jar", \
     "frontend.jar"]
