#!/bin/bash

# Start SSH daemon service
service ssh start

# Start Nginx in the background
nginx

# Run the Spring Boot application executable
java -jar /app/app.jar