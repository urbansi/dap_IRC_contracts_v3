#!/bin/bash

set -e

source ./venv/bin/activate

source ./scripts/util/dir.sh
source ./scripts/util/console.sh

checkWalletExists () {
  _network=$1
  _name=$2
  _path="${KEYSTORES_DIR}/${_network}/"
  _filepath="${_path}/${_name}.icx"
  
  mkdir -p ${_path}
  
  if [[ ! -f "$_filepath" ]]; then
    error "Keystore '${_name}' doesn't exist, please create one at the following path: \n${_filepath}"
    exit 1
  fi
}
