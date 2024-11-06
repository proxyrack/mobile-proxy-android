FROM saschpe/android-ndk:35-jdk22.0.2_9-ndk27.0.12077973-cmake3.22.1

WORKDIR /app

ENV KEY_ALIAS=default_alias
ENV KEY_PASSWORD=default_password
ENV KEYSTORE_PASSWORD=default_password
ENV KEYSTORE_FILE_PATH=/keystore.jks

RUN keytool -genkeypair -v -keystore $KEYSTORE_FILE_PATH -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD -keyalg RSA -keysize 2048 -validity 10000 -alias $KEY_ALIAS -dname "CN=Your Name, OU=Your Unit, O=Your Organization, L=Your City, ST=Your State, C=Your Country Code"

ENTRYPOINT ["./gradlew"]
CMD ["assembleRelease"]