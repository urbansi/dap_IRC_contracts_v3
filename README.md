# DAP Java SCORE 

## Requirements

### WSL on Windows

In order to run the scripts on Windows, you'll need to [install WSL](https://learn.microsoft.com/en-us/windows/wsl/install).
Once installed, you may run the scripts on Windows as if you were on Linux.

### Software requirements

- Open a new WSL console prompt.
- Run the following command in order to install Python3, pip, JDK11 and jq.

```bash
$ sudo apt install python3 python3-pip openjdk-11-jdk jq
```

## Setup the build system

We'll install the python packages dependancies in a `virtualenv`:

```bash
$ # Run this in the root folder of the project
$ python3 -m venv ./venv
$ source ./venv/bin/activate
$ pip install -r ./requirements.txt
```

Please make sure your virtualenv is enabled when deploying & updating new contracts.

## Run unit tests

```bash
$ ./gradlew test
```

If everything went successfully, it will end with the following output:

`BUILD SUCCESSFUL in ...s`

## Prepare the operator wallet

Before updating your contracts, you'll need to copy & paste your operator keystore in the right folder.

- The operator key needs to be the same key than the one which deployed the Python tokens, otherwise the deployment will fail.

- Please put your operator keystore in the following path:

`./scripts/config/keystores/mainnet/operator.icx`

- Please note that you'll need to rename your operator keystore with the following name: `operator.icx`

The update scripts check if this file is correctly set before deployment, so there's no risk if you don't follow this step correctly.

## How to update the Python contracts on MainNet

The following scripts are used in a similar manner:

```bash
$ ./scripts/scenario/update/<script>.sh <network> <target score to update>
```

Here's what you'll need to execute for updating the Python scripts on MainNet :
(please double check for any error)

```bash
./scripts/scenario/update/1.update-CRI10X_Token_V2.sh mainnet cx7e7851253fcde669a0b9d9cf55db5146d619ac7b
./scripts/scenario/update/2.update-EURD_Token_V2.sh mainnet cx96f98f59f1dc144709fd294d4b906c74f5d709b1
./scripts/scenario/update/3.update-LEND5_Token_V2.sh mainnet cx90b9d86f545329831e22a1df4b0618904d3a2d67
./scripts/scenario/update/4.update-MRPRO_Token_V2.1.sh mainnet cx21712833c4fb36764bf9f3f30f41b905f55b1441
./scripts/scenario/update/5.update-MRS_Token_V2.sh mainnet cx7e618534f5d96a25b15c50d1a70b74550fbc8a87
```

This command will update the Python DAP Tokens contracts on MainNet with their Java contracts equivalent.