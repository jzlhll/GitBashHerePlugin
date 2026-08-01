#!/usr/bin/env bash

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
DOWNLOADS_DIR="$HOME/Downloads"
DISTRIBUTIONS_DIR="$PROJECT_DIR/build/distributions"

cd "$PROJECT_DIR"

if [[ -d "$DISTRIBUTIONS_DIR" ]]; then
    echo "Cleaning previous plugin archives..."
    find "$DISTRIBUTIONS_DIR" -maxdepth 1 -type f -name '*.zip' -delete
fi

sh ./gradlew buildPlugin

PROJECT_NAME="$(sed -nE 's/^rootProject.name = "([^"]+)"/\1/p' settings.gradle | head -n 1)"
PROJECT_VERSION="$(sed -nE 's/^version = "([^"]+)"/\1/p' build.gradle.kts | head -n 1)"
ARCHIVE_PATH="$DISTRIBUTIONS_DIR/${PROJECT_NAME}-${PROJECT_VERSION}.zip"

if [[ -z "$PROJECT_NAME" || -z "$PROJECT_VERSION" || ! -f "$ARCHIVE_PATH" ]]; then
    echo "Plugin archive not found: $ARCHIVE_PATH" >&2
    exit 1
fi

if [[ ! -d "$DOWNLOADS_DIR" ]]; then
    echo "Downloads directory not found: $DOWNLOADS_DIR" >&2
    exit 1
fi

cp "$ARCHIVE_PATH" "$DOWNLOADS_DIR/"
echo "Copied plugin archive to: $DOWNLOADS_DIR/$(basename "$ARCHIVE_PATH")"
