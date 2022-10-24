#!/bin/bash

set -e

source ./venv/bin/activate

source ./scripts/util/get_address.sh
source ./scripts/util/dir.sh
source ./scripts/util/console.sh
source ./scripts/util/env.sh
source ./scripts/util/check_wallet.sh

if [ "$#" -ne "2" ] ; then
  error "Usage: $0 <network> <score address to update>"
  exit 1
fi

network=$1
scoreAddress=$2

info "LEND5_Token_V2 will be updated on ${network} at ${scoreAddress}."
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
pkg="LEND5_Token_V2"
javaPkg=":LEND5_Token_V2:Contract"
build="optimized"

# Updating
info "Updating ${pkg}..."

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


deployJson="${deployDir}/deploy.json"
(cat ${deployJson}) | jq ".scoreAddress |= \"${scoreAddress}\"" | jq '.' > /tmp/output
mv /tmp/output ${deployJson}

python run.py -e ${network} update ${pkg}

success "${pkg} has been successfully updated!"