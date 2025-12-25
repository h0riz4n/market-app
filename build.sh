chmod +x ./gradlew
./gradlew clean build
export $(grep -v '^#' .env | xargs)
docker-compose up -d --build
