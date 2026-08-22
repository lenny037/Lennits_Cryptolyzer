#!/usr/bin/env bash

################################################################################
# LENNIT'S CRYPTOLYZER — QUICK START GUIDE
# Installation and execution instructions
################################################################################

# FOR LINUX / macOS / Termux
# ============================================================================

## STEP 1: Clone the main repository
git clone https://github.com/lenny037/Lennits_Cryptolyzer.git
cd Lennits_Cryptolyzer

## STEP 2: Make the extraction script executable
chmod +x extract-and-push.sh

## STEP 3: Install prerequisites (if needed)

### On Ubuntu/Debian:
sudo apt-get update
sudo apt-get install -y git p7zip-full

### On macOS:
brew install git p7zip

### On Termux (Android):
pkg update
pkg install -y git p7zip

## STEP 4: Ensure git is configured with your identity
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

## STEP 5: Run the extraction script
./extract-and-push.sh

## The script will:
# 1. Detect split zip archives (zip.001 through zip.006)
# 2. Extract all files using 7z
# 3. Organize files by type (python/, typescript/, kotlin/, rust/, etc.)
# 4. Create a new git branch: 'extracted-content'
# 5. Commit all organized files
# 6. Push to GitHub

## Expected output:
# ✓ Archives extracted successfully
# ✓ Files organized in extracted_source
# ✓ Successfully pushed to GitHub!
# ✓ View at: https://github.com/lenny037/Lennits_Cryptolyzer/tree/extracted-content

################################################################################

# FOR WINDOWS
# ============================================================================

## STEP 1: Install prerequisites
# - Git: https://git-scm.com/download/win
# - 7-Zip: https://www.7-zip.org/download.html

## STEP 2: Clone the repository
git clone https://github.com/lenny037/Lennits_Cryptolyzer.git
cd Lennits_Cryptolyzer

## STEP 3: Configure git
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

## STEP 4: Run the batch script
extract-and-push.bat

## The script will:
# 1. Detect split zip archives
# 2. Extract files using 7-Zip
# 3. Organize by type
# 4. Create 'extracted-content' branch
# 5. Commit and push to GitHub

################################################################################

# TROUBLESHOOTING
# ============================================================================

# If extraction fails on Linux/Mac:
# - Install p7zip: brew install p7zip (macOS) or apt install p7zip-full (Linux)
# - Or use unzip: unzip "Lennits_Cryotolyzer archives.zip.001"

# If git push fails:
# - Check credentials: git config --list
# - Ensure SSH key is set up or use HTTPS token
# - Try: git remote set-url origin https://YOUR_TOKEN@github.com/lenny037/Lennits_Cryptolyzer.git

# If files don't organize properly:
# - Check folder permissions: chmod -R 755 extracted_source/
# - Manually move files if needed

# To verify extraction worked:
find extracted_source -type f | wc -l  # Count all files
ls -lah extracted_source/*/           # Show file counts by type

################################################################################

# NEXT STEPS FOR COPILOT
# ============================================================================

# Once the script completes successfully:

# 1. Tell Copilot:
#    "Branch 'extracted-content' is ready for processing"
#
# 2. Copilot will:
#    - Read all files from the GitHub branch
#    - Analyze code structure and dependencies
#    - Extract architecture patterns
#    - Generate elite reconstruction
#    - Create new canonical repository
#
# 3. Estimated time: 3-4 hours for full platform build

################################################################################
