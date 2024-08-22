FROM bellsoft/liberica-openjdk-alpine:17.0.12

# 디렉토리 생성 및 이동
WORKDIR /app

# 호스트 파일을 도커 컨테이너로 복사(실행 전 jar 파일 생성 필요)
COPY ./build/libs/festivals-0.0.1-SNAPSHOT.jar .

# 도커 컨테이너에서 실행할 명령어 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "festivals-0.0.1-SNAPSHOT.jar"]
