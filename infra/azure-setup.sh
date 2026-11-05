#!/usr/bin/env bash
# One-time Azure provisioning for the CIA 2 pipeline (run after `az login`).
# Creates: resource group, Azure Container Registry (Basic), AKS cluster (Free tier, 1 small node).
set -euo pipefail

RG=rg-cia2
LOCATION=${LOCATION:-centralindia}
ACR=${ACR:-ciaacr$RANDOM}
AKS=aks-cia2

az provider register --namespace Microsoft.ContainerService --wait
az provider register --namespace Microsoft.ContainerRegistry --wait

az group create -n $RG -l $LOCATION
az acr create -g $RG -n $ACR --sku Basic --admin-enabled true
az aks create -g $RG -n $AKS --tier free --node-count 1 --node-vm-size Standard_B2s \
  --generate-ssh-keys --enable-managed-identity

# kubeconfig for the pipelines (stored as a GitHub secret, never committed)
az aks get-credentials -g $RG -n $AKS --file kubeconfig-aks --overwrite-existing
echo "ACR login server: $(az acr show -n $ACR --query loginServer -o tsv)"

# Clean-up when the demo is over (stops all charges):
#   az group delete -n rg-cia2 --yes --no-wait
