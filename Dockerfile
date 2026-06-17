FROM ubuntu:24.04

# Avoid prompts during installation
ENV DEBIAN_FRONTEND=noninteractive

# Install OpenJDK 21, Nginx, OpenSSH Server, and Curl
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
    nginx \
    openssh-server \
    php-cli \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Configure SSH daemon settings for Port 2222 with explicit credentials
RUN mkdir /var/run/sshd \
    && echo 'root:Hello@123' | chpasswd \
    && sed -i 's/#Port 22/Port 2222/' /etc/ssh/sshd_config \
    && sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# Remove default Nginx config and copy our proxy setup
RUN rm /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# Set up working app directory
WORKDIR /app

# Copy your compiled Spring Boot application JAR from the local target directory
COPY target/*.jar /app/app.jar

# Copy and set execution permission for the startup entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose required assignment ports
EXPOSE 8443 2222

ENTRYPOINT ["/entrypoint.sh"]