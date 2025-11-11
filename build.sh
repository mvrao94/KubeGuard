#!/bin/bash
# Convenience script for building the project
# This maintains backward compatibility while using the organized structure

echo "Building KubeGuard..."
./build-tools/mvnw clean package "$@"