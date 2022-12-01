#!/bin/bash

set -e

source ./venv/bin/activate

source ./scripts/util/get_address.sh
source ./scripts/util/dir.sh
source ./scripts/util/console.sh
source ./scripts/util/env.sh
source ./scripts/util/check_wallet.sh

if [ "$#" -ne "1" ] ; then
  error "Usage: $0 <network>"
  exit 1
fi

network=$1

info "CRI10X_Token_V2 will be deployed on ${network}."
read -p "Confirm (y/n) ? " choice
case "$choice" in 
  y|Y ) ;;
  * ) exit;;
esac

# Requirements
checkWalletExists ${network} operator

# Start
info "Cleaning..."
./gradlew clean > /dev/null

# Package information
pkg="CRI10X_Token_V2"
javaPkg=":CRI10X_Token_V2:Contract"
build="optimized"

# Deploying
info "Deploying ${pkg}..."

# Setup packages
setupJavaDir ${pkg} ${javaPkg} ${build}
setupDeployDir ${pkg} ${network}
setupCallsDir ${pkg} ${network}
deployDir=$(getDeployDir ${pkg} ${network})
callsDir=$(getCallsDir ${pkg} ${network})

_decimals=0x12

# Deploy on ICON network
filter=$(cat <<EOF
{
  _decimals: \$_decimals
}
EOF
)

jq -n \
  --arg _decimals $_decimals \
  "${filter}" > ${deployDir}/params.json

python run.py -e ${network} deploy ${pkg}

success "${pkg} has been successfully deployed!"