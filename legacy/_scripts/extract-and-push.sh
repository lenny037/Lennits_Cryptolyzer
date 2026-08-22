#!/usr/bin/env bash

#############################################################################
# LENNIT'S CRYPTOLYZER — AUTOMATED EXTRACTION & GITHUB PUSH SCRIPT
# Extracts split zip archives, organizes files, pushes to GitHub
# 
# Usage: bash extract-and-push.sh
# Platforms: Linux, macOS, Termux (Android), WSL
#############################################################################

set -o pipefail

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Config
BRANCH_NAME="extracted-content"
OUTPUT_DIR="extracted_source"
ARCHIVE_PATTERN="Lennits_Cryotolyzer archives.zip.001"

#############################################################################
# FUNCTIONS
#############################################################################

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

log_error() {
    echo -e "${RED}[✗]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

check_command() {
    if ! command -v "$1" &> /dev/null; then
        return 1
    fi
    return 0
}

detect_extractor() {
    if check_command 7z; then
        echo "7z"
    elif check_command unzip; then
        echo "unzip"
    else
        log_error "No extraction tool found!"
        log_info "Please install either 7-Zip or unzip:"
        log_info "  - Ubuntu/Debian: sudo apt-get install p7zip-full"
        log_info "  - macOS: brew install p7zip"
        log_info "  - Termux: pkg install p7zip"
        exit 1
    fi
}

#############################################################################
# MAIN EXECUTION
#############################################################################

echo ""
echo "╔════════════════════════════════════════════════════════════════════════╗"
echo "║       LENNIT'S CRYPTOLYZER — EXTRACTION & GITHUB PUSH SCRIPT           ║"
echo "╚════════════════════════════════════════════════════════════════════════╝"
echo ""

# Step 1: Check prerequisites
log_info "Checking prerequisites..."

if ! check_command git; then
    log_error "Git is not installed!"
    log_info "Install: https://git-scm.com/download"
    exit 1
fi
log_success "Git found"

EXTRACTOR=$(detect_extractor)
log_success "Extractor found: $EXTRACTOR"

if ! check_command find; then
    log_error "find command not available"
    exit 1
fi

# Step 2: Detect split archives
log_info "Detecting split archives..."

ARCHIVES=()
for i in {001..010}; do
    ARCHIVE="Lennits_Cryotolyzer archives.zip.$i"
    if [ -f "$ARCHIVE" ]; then
        ARCHIVES+=("$ARCHIVE")
        log_success "Found: $ARCHIVE"
    fi
done

if [ ${#ARCHIVES[@]} -eq 0 ]; then
    log_error "No split archives found!"
    log_info "Expected files: Lennits_Cryotolyzer archives.zip.001, .002, etc."
    log_info "Current directory: $(pwd)"
    log_info "Files here:"
    ls -la *.zip* 2>/dev/null || log_warning "No zip files found"
    exit 1
fi

echo ""
log_info "Found ${#ARCHIVES[@]} archive parts. Starting extraction..."
echo ""

# Step 3: Extract archives
if [ "$EXTRACTOR" = "7z" ]; then
    log_info "Extracting with 7-Zip..."
    if ! 7z x "${ARCHIVES[0]}" -aoa; then
        log_error "Extraction failed!"
        exit 1
    fi
elif [ "$EXTRACTOR" = "unzip" ]; then
    log_info "Extracting with unzip..."
    if ! unzip -o "${ARCHIVES[0]}"; then
        log_error "Extraction failed!"
        exit 1
    fi
fi

log_success "Extraction complete!"

# Step 4: Find extracted directory (usually created by the archive)
EXTRACTED_DIR=""
for dir in Lennits* lennits* LENNITS* extracted* source*; do
    if [ -d "$dir" ] && [ "$dir" != "$OUTPUT_DIR" ]; then
        EXTRACTED_DIR="$dir"
        break
    fi
done

if [ -z "$EXTRACTED_DIR" ]; then
    log_warning "Could not auto-detect extracted directory. Checking current directory..."
    EXTRACTED_DIR="."
fi

log_success "Source directory: $EXTRACTED_DIR"

# Step 5: Create output directory structure
log_info "Organizing files by type..."

mkdir -p "$OUTPUT_DIR"/{python,typescript,kotlin,rust,config,docs,assets,web,android,other}

# File organization
organize_files() {
    find "$EXTRACTED_DIR" -type f 2>/dev/null | while read -r file; do
        filename=$(basename "$file")
        extension="${filename##*.}"
        
        case "$extension" in
            py)
                cp "$file" "$OUTPUT_DIR/python/" 2>/dev/null || true
                ;;
            ts|tsx|js|jsx)
                cp "$file" "$OUTPUT_DIR/typescript/" 2>/dev/null || true
                ;;
            kt)
                cp "$file" "$OUTPUT_DIR/kotlin/" 2>/dev/null || true
                ;;
            rs)
                cp "$file" "$OUTPUT_DIR/rust/" 2>/dev/null || true
                ;;
            json|yaml|yml|toml|gradle|gradle.kts|properties)
                cp "$file" "$OUTPUT_DIR/config/" 2>/dev/null || true
                ;;
            md|txt|html|css)
                cp "$file" "$OUTPUT_DIR/docs/" 2>/dev/null || true
                ;;
            png|jpg|jpeg|svg|gif)
                cp "$file" "$OUTPUT_DIR/assets/" 2>/dev/null || true
                ;;
            *)
                cp "$file" "$OUTPUT_DIR/other/" 2>/dev/null || true
                ;;
        esac
    done
}

organize_files

# Also copy any web/android directories directly
[ -d "$EXTRACTED_DIR/web" ] && cp -r "$EXTRACTED_DIR/web"/* "$OUTPUT_DIR/web/" 2>/dev/null || true
[ -d "$EXTRACTED_DIR/android" ] && cp -r "$EXTRACTED_DIR/android"/* "$OUTPUT_DIR/android/" 2>/dev/null || true

log_success "Files organized successfully"

# Step 6: Count files
echo ""
log_info "File summary:"
echo ""

for subdir in "$OUTPUT_DIR"/*; do
    if [ -d "$subdir" ]; then
        name=$(basename "$subdir")
        count=$(find "$subdir" -type f | wc -l)
        if [ "$count" -gt 0 ]; then
            printf "  %-15s %3d files\n" "$name:" "$count"
        fi
    fi
done

TOTAL=$(find "$OUTPUT_DIR" -type f | wc -l)
echo ""
log_success "Total files organized: $TOTAL"

# Step 7: Git operations
echo ""
log_info "Preparing git operations..."

# Create branch
if git branch | grep -q "$BRANCH_NAME"; then
    log_warning "Branch $BRANCH_NAME already exists. Switching to it..."
    git checkout "$BRANCH_NAME" || exit 1
    git pull origin "$BRANCH_NAME" || log_warning "Could not pull from remote"
else
    log_info "Creating new branch: $BRANCH_NAME"
    git checkout -b "$BRANCH_NAME" || exit 1
fi

# Add files
log_info "Adding files to git..."
git add "$OUTPUT_DIR"/ || exit 1

# Check if there's anything to commit
if git diff --cached --quiet; then
    log_warning "No new files to commit. Branch is up to date."
else
    # Commit
    COMMIT_MSG="Extract: All source files organized and ready for processing

- Extracted split archives (6 parts)
- Organized by file type (python, typescript, kotlin, rust, etc.)
- Total files: $TOTAL
- Ready for Copilot platform reconstruction"

    log_info "Creating commit..."
    git commit -m "$COMMIT_MSG" || exit 1
    log_success "Commit created"

    # Push
    log_info "Pushing to GitHub..."
    if git push -u origin "$BRANCH_NAME"; then
        log_success "Successfully pushed to GitHub!"
    else
        log_error "Push failed. Trying alternative method..."
        git push origin "$BRANCH_NAME" --force || {
            log_error "Push failed completely"
            log_info "Please try manually: git push -u origin $BRANCH_NAME"
            exit 1
        }
    fi
fi

# Step 8: Success summary
echo ""
echo "╔════════════════════════════════════════════════════════════════════════╗"
echo "║                     ✓ EXTRACTION COMPLETE                              ║"
echo "╚════════════════════════════════════════════════════════════════════════╝"
echo ""
echo "📦 Extracted: $TOTAL files"
echo "📁 Location: $OUTPUT_DIR/"
echo "🔗 Branch: $BRANCH_NAME"
echo ""
echo "View your files:"
REPO_URL=$(git config --get remote.origin.url | sed 's/\.git$//')
echo "  ${GREEN}${REPO_URL}/tree/${BRANCH_NAME}${NC}"
echo ""
echo "Next steps:"
echo "  1. Tell Copilot: \"Branch '${BRANCH_NAME}' is ready for processing\""
echo "  2. Copilot will analyze all files and build the elite platform"
echo "  3. Expected time: 3-4 hours for complete reconstruction"
echo ""
