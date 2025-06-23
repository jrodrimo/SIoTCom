#!/bin/bash

# --- CONFIGURATION ---
FILES_DIR="/home/bc/Downloads/junio"

# --- CHECK: was a date provided? ---
if [ -z "$1" ]; then
    echo "❌ Usage: ./upload_and_display_cids.sh <target-date>"
    echo "   Example: ./upload_and_display_cids.sh 2024-june-1"
    exit 1
fi

TARGET_DATE="$1"

# --- CHECK IPFS DAEMON ---
if ! pgrep -x "ipfs" > /dev/null; then
    echo "❌ IPFS daemon not running. Start it with: ipfs daemon"
    exit 1
fi

# --- PROCESS FILES ---
echo "📂 Looking for files in $FILES_DIR matching date: $TARGET_DATE"
echo

i=1
for file in "$FILES_DIR"/*; do
    if [[ -f "$file" && "$file" == *"$TARGET_DATE"* ]]; then
        filename=$(basename "$file")
        echo "📤 Uploading: $filename"

        cid=$(ipfs add -q "$file")
        if [ -n "$cid" ]; then
            padded_id=$(printf "%03d" $i)
            echo "✅ Upload complete!"
            echo "   📎 File Name: $filename"
            echo "   🔗 CID: $cid"
            echo "   🆔 Suggested Asset ID: ipfsFile$padded_id"
            echo
            ((i++))
        else
            echo "❌ Failed to upload $filename"
        fi
    fi
done

if [ "$i" -eq 1 ]; then
    echo "⚠️ No files found containing '$TARGET_DATE'."
else
    echo "✅ Done uploading all matching files to IPFS."
fi
