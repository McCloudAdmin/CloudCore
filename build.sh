#!/bin/bash

# Build all modules
mvn clean package -X

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi
killall java

# Clean up old files
rm -f CloudCore.jar
rm -rf ./tests/bungee/plugins/CloudCore.jar
rm -rf ./tests/velo/plugins/CloudCore.jar
rm -rf ./tests/bungee/plugins/CloudCore
rm -rf ./tests/velo/plugins/cloudcore
rm -rf ./tests/spigot/plugins/CloudCore.jar
rm -rf ./tests/spigot/plugins/CloudCore

# Copy platform-specific JARs to their respective test directories
cp ./velocity/target/cloudcore-velocity-*-SNAPSHOT.jar ./tests/velo/plugins/CloudCore.jar
cp ./bungeecord/target/cloudcore-bungeecord-*-SNAPSHOT.jar ./tests/bungee/plugins/CloudCore.jar
cp ./spigot/target/cloudcore-spigot-*-SNAPSHOT.jar ./tests/spigot/plugins/CloudCore.jar

echo "Build complete!"
echo "JARs are available at:"
echo "- Velocity: ./tests/velo/plugins/CloudCore.jar"
echo "- BungeeCord: ./tests/bungee/plugins/CloudCore.jar"
echo "- Spigot: ./tests/spigot/plugins/CloudCore.jar"

# Check if server JARs exist
if [ ! -f "./tests/bungee/BungeeCord.jar" ]; then
    echo "Error: BungeeCord.jar not found in ./tests/bungee/"
    exit 1
fi

if [ ! -f "./tests/velo/Velocity.jar" ]; then
    echo "Error: Velocity.jar not found in ./tests/velo/"
    exit 1
fi

if [ ! -f "./tests/spigot/FlamePaper.jar" ]; then
    echo "Error: FlamePaper.jar not found in ./tests/spigot/"
    exit 1
fi

echo "Running tests..."
cd ./tests/bungee
java -jar BungeeCord.jar &
BUNGEE_PID=$!
cd ../velo
java -jar Velocity.jar &
VELOCITY_PID=$!
cd ../spigot
java -jar FlamePaper.jar &
SPIGOT_PID=$!
cd ../..

echo "Tests started!"
echo "Press Ctrl+C to stop all test servers"

# Function to handle cleanup on script exit
cleanup() {
    echo "Shutting down test servers..."
    kill $BUNGEE_PID $VELOCITY_PID $SPIGOT_PID 2>/dev/null
    exit 0
}

# Set up trap for cleanup
trap cleanup SIGINT SIGTERM

# Wait for user to press Ctrl+C
wait