#!/bin/bash

# MCCloudAdmin Chat Worker Startup Script

# Set Java options
JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"

# Set the config file path
CONFIG_FILE="config.yml"

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Config file $CONFIG_FILE not found. The worker will create a default config on first run."
fi

# Build the project if needed
if [ ! -f "target/mccloudadmin-worker-1.0-SNAPSHOT.jar" ]; then
    echo "Building worker project..."
    mvn clean package
fi

# Start the worker
echo "Starting MCCloudAdmin Chat Worker..."
java $JAVA_OPTS -jar target/mccloudadmin-worker-1.0-SNAPSHOT.jar "$CONFIG_FILE" 