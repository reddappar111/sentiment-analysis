# Use an official OpenJDK runtime as a parent image
FROM openjdk:17

# Set the working directory in the container
WORKDIR /app

EXPOSE 8080
# Copy the JAR file into the container
COPY target/sentiment-analysis-0.0.1-SNAPSHOT.jar sentiment-analysis.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "sentiment-analysis.jar"]
