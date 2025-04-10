# 指定应用基础依赖
FROM openjdk:8
# 添加容器目录
COPY bee-3.9.0.jar /app.jar
EXPOSE 8000
# 指定应用启动命令
RUN echo "java -jar -Dspring.profiles.active=dev \
         -Xms2048m -Xmx2048m \
         -XX:MetaspaceSize=512m \
         -XX:MaxMetaspaceSize=512m \
         -XX:+UseParNewGC -XX:+UseConcMarkSweepGC \
         -XX:+PrintGCDetails \
         -XX:+PrintGCDateStamps \
         -Xloggc:/data/robot/logs/dev/admin-gc.log \
         -XX:NumberOfGCLogFiles=5 \
         -XX:GCLogFileSize=100M" >> /run.sh
ENTRYPOINT ["java", "-jar", "/app.jar","/run.sh"]
