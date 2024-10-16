FROM saschpe/android-ndk:35-jdk22.0.2_9-ndk27.0.12077973-cmake3.22.1

WORKDIR /app

ENTRYPOINT ["./gradlew"]