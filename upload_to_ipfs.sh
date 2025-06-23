#!/bin/bash

FILES_DIR="/home/bc/Downloads/junio"
echo "Uploading files from: $FILES_DIR"

# Check if IPFS daemon is running
if ! pgrep -x "ipfs" > /dev/null; then
  echo "IPFS daemon not running. Please start it with: ipfs daemon"
  exit 1
fi

i=1

for file in "$FILES_DIR"/*; do
  if [ -f "$file" ]; then
    echo "📤 Uploading file: $file"

    # Upload to IPFS and get CID
    cid=$(ipfs add -q "$file")
    
    if [ -z "$cid" ]; then
      echo "❌ Failed to upload $file to IPFS."
      continue
    fi

    padded=$(printf "%03d" $i)
    assetID="ipfsFile$padded"
    owner="autoUploader"

    echo "File '$file' uploaded successfully."
    echo "Asset ID: $assetID | CID: $cid"
    echo "To query in Fabric: peer chaincode query -C mychannel -n basic -c '{\"Args\":[\"ReadAsset\",\"$assetID\"]}'"
    echo "To access in IPFS: ipfs cat $cid or open http://127.0.0.1:8080/ipfs/$cid"
    echo

    # Optional: Save the asset ID and CID to a file
    echo "$assetID,$cid" >> ipfs_uploaded_files.csv

    # Optional: Auto-add to Fabric (uncomment if needed)
    # peer chaincode invoke \
    #   -o localhost:7050 \
    #   --ordererTLSHostnameOverride orderer.example.com \
    #   --tls \
    #   --cafile "$ORDERER_CA" \
    #   -C mychannel \
    #   -n basic \
    #   --peerAddresses localhost:7051 \
    #   --tlsRootCertFiles "$CORE_PEER_TLS_ROOTCERT_FILE" \
    #   -c "{\"function\":\"CreateAsset\",\"Args\":[\"$assetID\",\"$cid\",\"0\",\"$owner\",\"0\",\"true\"]}"

    ((i++))
    sleep 0.5
  fi
done

echo "All files uploaded. CIDs saved in ipfs_uploaded_files.csv"
