FROM openjdk:8-jre-alpine

# Add a jhipster user to run our application so that it doesn't need to run as root
# RUN adduser -D -s /bin/sh jhipster
# WORKDIR /home/jhipster

# ADD entrypoint.sh entrypoint.sh
# RUN chmod 755 entrypoint.sh && chown jhipster:jhipster entrypoint.sh
# USER jhipster

# ENTRYPOINT ["./entrypoint.sh"]

COPY build/libs/*.jar /app.jar

CMD java ${JAVA_OPTS} -jar /app.jar
