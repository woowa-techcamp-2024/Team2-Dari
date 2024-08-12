FROM eclipse-temurin:17-jre-alpine

CMD ["./gradlew", "build"]

# 앱 디렉토리 생성
RUN mkdir /app

# 앱 디렉토리로 이동
WORKDIR /app

# 호스트의 build/libs 디렉토리에 있는 jar 파일을 컨테이너의 app 디렉토리로 복사
COPY build/libs/*.jar app.jar

# 컨테이너 실행 시 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
