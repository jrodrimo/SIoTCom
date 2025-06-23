#!/bin/bash

# Directory with the files to upload
FILES_DIR="/home/bc/Downloads/junio"

# Fabric setup
cd ~/fabric-samples/test-network || { echo "Fabric network not found!"; exit 1; }

export PATH=${PWD}/../bin:$PATH
export FABRIC_CFG_PATH=${PWD}/../config/
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051
export ORDERER_CA=${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

echo "Uploading files from: $FILES_DIR"
i=1

for file in "$FILES_DIR"/*; do
  if [ -f "$file" ]; then
    echo "Uploading file: $file"
    cid=$(ipfs add -q "$file")

    if [ -z "$cid" ]; then
      echo "Failed to upload $file to IPFS."
      continue
    fi

    padded=$(printf "%03d" $i)
    assetID="ipfsFile$padded"
    owner="autoUploader"
    echo "CID: $cid → Registering as Asset ID: $assetID"

    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "$ORDERER_CA" \
      -C mychannel \
      -n basic \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "$CORE_PEER_TLS_ROOTCERT_FILE" \
      -c "{\"function\":\"CreateAsset\",\"Args\":[\"$assetID\",\"$cid\",\"0\",\"$owner\",\"0\",\"true\"]}"

    sleep 1  # Wait for commit
    ((i++))
  fi
done

echo "All files processed. Run this to verify:"
echo "peer chaincode query -C mychannel -n basic -c '{\"Args\":[\"GetAllAssets\"]}'"
