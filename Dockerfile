# 指定应用基础依赖
FROM openjdk:8
# 添加容器目录
COPY bee-3.9.0.jar /app.jar
EXPOSE 8000

# 创建日志目录并设置权限
RUN mkdir -p /data/robot/logs/prod && \
    chmod -R 777 /data/robot/logs  # 确保目录可写

# 生成启动脚本（修正Java命令格式）
RUN echo '#!/bin/sh' > /run.sh && \
    echo 'java -jar /app.jar \
         -Dspring.profiles.active=prod \
         -Xms2048m -Xmx2048m \
         -XX:MetaspaceSize=512m \
         -XX:MaxMetaspaceSize=512m \
         -XX:+UseParNewGC -XX:+UseConcMarkSweepGC \
         -XX:+PrintGCDetails \
         -XX:+PrintGCDateStamps \
         -Xloggc:/data/robot/logs/prod/admin-gc.log \
         -XX:NumberOfGCLogFiles=5 \
         -XX:GCLogFileSize=100M' >> /run.sh && \
    chmod +x /run.sh

# 启动脚本
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["/bin/sh", "/run.sh"]