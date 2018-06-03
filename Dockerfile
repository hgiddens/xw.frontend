FROM cloudscala/scala-graalvm:scala-2.12.6-sbt-1.1.5-graalvm-1.0.0-rc1 AS build

LABEL maintainer "hgiddens@gmail.com"

COPY . /src

WORKDIR /src

RUN sbt -batch server/assembly

FROM cloudscala/scala-graalvm:scala-2.12.6-sbt-1.1.5-graalvm-1.0.0-rc1 AS run

LABEL maintainer "hgiddens@gmail.com"

EXPOSE 8080

WORKDIR /app

COPY --from=build /src/server/target/scala-2.12/frontend.jar /app/frontend.jar

RUN chmod o+rx /root && chmod -R o+rX /root/graalvm-1.0.0-rc1

USER nobody

CMD ["java", \
     "-XX:+UseG1GC", \
     "-XX:+UnlockExperimentalVMOptions", \
     "-XX:+UseCGroupMemoryLimitForHeap", \
     "-jar", \
     "frontend.jar"]
