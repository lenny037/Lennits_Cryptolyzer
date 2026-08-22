#!/bin/bash

echo "Initializing Lennit Cryptolyzer Firebase System..."

firebase login
firebase init functions firestore hosting

mkdir -p functions/src/modules
mkdir -p functions/src/core
mkdir -p functions/src/shared
mkdir -p scripts
mkdir -p infra

echo "Scaffold generated."
