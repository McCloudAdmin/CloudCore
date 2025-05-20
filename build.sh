#!/bin/bash

# Build the universal JAR
mvn clean package

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

rm CloudCore.jar 


# The output JAR will be in bootstrap/target/
# Copy it to a more convenient location
cp bootstrap/target/CloudCore-*-SNAPSHOT.jar ./CloudCore.jar

echo ""
echo "Build complete!"
echo "Universal JAR is available at: ./CloudCore.jar"
echo "This single JAR works on both BungeeCord and Velocity platforms." 
rm -rf ./tests/bungee/plugins/CloudCore.jar
rm -rf ./tests/velocity/plugins/CloudCore.jar
rm -rf ./tests/bungee/plugins/CloudCore
rm -rf ./tests/velo/plugins/cloudcore
rm -rf ./tests/spigot/plugins/CloudCore.jar
rm -rf ./tests/spigot/plugins/CloudCore

cp ./CloudCore.jar ./tests/bungee/plugins/CloudCore.jar
cp ./CloudCore.jar ./tests/velo/plugins/CloudCore.jar
cp ./CloudCore.jar ./tests/spigot/plugins/CloudCore.jar

echo "Copied to tests/bungee/plugins/CloudCore.jar"
echo "Copied to tests/velo/plugins/CloudCore.jar"
echo "Copied to tests/spigot/plugins/CloudCore.jar"

echo "Build complete!"

echo "Running tests..."
cd ./tests/bungee
java -jar BungeeCord.jar
cd ..
cd velo
java -jar Velocity.jar
cd ..

cd spigot
java -jar FlamePaper.jar
cd ..

echo "Tests complete!"