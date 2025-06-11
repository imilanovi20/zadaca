#!/bin/bash

echo "=== Starting Maven build and deploy process ==="
echo "Timestamp: $(date)"
echo

# Korak 1: Clean i package
echo "Step 1: Running mvn clean package..."
mvn clean package

# Provjeri je li build uspješan
if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    echo
else
    echo "✗ Build failed! Stopping deployment."
    exit 1
fi

# Korak 2: Redeploy
echo "Step 2: Running mvn cargo:redeploy..."
mvn cargo:redeploy -P ServerEE-local

# Provjeri je li deployment uspješan
if [ $? -eq 0 ]; then
    echo "✓ Deployment successful!"
    echo
    echo "=== Process completed successfully ==="
else
    echo "✗ Deployment failed!"
    exit 1
fi

echo "Finished at: $(date)"
