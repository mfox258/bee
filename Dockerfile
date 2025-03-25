# 指定应用基础依赖
FROM openjdk:8
# 作者
MAINTAINER mfox
# 使用 root用户
USER root
# 添加容器目录
COPY target/bee-3.9.0.jar /app.jar
# 指定应用启动命令
RUN echo "java -jar -Dspring.profiles.active=prod \
                    -Xms2048m -Xmx2048m \
                    -XX:MetaspaceSize=512m \
                    -XX:MaxMetaspaceSize=512m \
                    -XX:+UseParNewGC -XX:+UseConcMarkSweepGC \
                    -XX:+PrintGCDetails \
                    -XX:+PrintGCDateStamps \
                    -Xloggc:/data/robot/logs/dev/admin-gc.log \
                    -XX:NumberOfGCLogFiles=5 \
                    -XX:GCLogFileSize=100M \
ENTRYPOINT ["java", "-jar", "/app.jar"]
