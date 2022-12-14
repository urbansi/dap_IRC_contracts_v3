#!/usr/bin/env python3

import argparse
from bash import bash

from scripts.contract.config import Config
from scripts.contract.deploy_contract import deploy, update
from scripts.contract.call_contract import invoke, call
from scripts.contract.meta import get_meta
from os.path import exists
import getpass

modules = [
]

class Command:

    def __init__(self) -> None:
        parser = argparse.ArgumentParser()
        parser.add_argument('-e', '--endpoint', type=str, default='gochain', help='target endpoint for connection')
        subparsers = parser.add_subparsers(title='Available commands', dest='command')
        subparsers.required = True

        deploy_parser = subparsers.add_parser('deploy', help='SCORE deploy operations')
        deploy_parser.add_argument('package', type=str, help='package name')

        update_parser = subparsers.add_parser('update', help='SCORE update operations')
        update_parser.add_argument('package', type=str, help='package name')

        invoke_parser = subparsers.add_parser('invoke', help='SCORE invokation operations')
        invoke_parser.add_argument('package', type=str, help='package name')
        invoke_parser.add_argument('params', type=str, help='contract params filename')

        call_parser = subparsers.add_parser('call', help='SCORE readonly call operations')
        call_parser.add_argument('package', type=str, help='package name')
        call_parser.add_argument('params', type=str, help='contract params filename')

        optimizedJar_parser = subparsers.add_parser('optimizedJar', help='Gradle optimizedJar operations')
        optimizedJar_parser.add_argument('package', type=str, help='package name')
        
        # add subcommand parsers
        for mod in modules:
            mod.add_parser(self, subparsers)

        args = parser.parse_args()
        class KeystoreArg:
            name: str
            def __init__(self, name):
                self.name = name

        args.keystore = KeystoreArg(Command.get_keystore_path(args.endpoint))
        getattr(self, args.command)(args)

    @staticmethod
    def get_keystore_path(network):
        return f"scripts/config/keystores/{network}/operator.icx"

    @staticmethod
    def get_keystore_password(network):
        target_path = f"scripts/config/keystores/{network}/operator.pwd"
        if exists(target_path):
            return open(target_path, "r").read()
        else:
            return getpass.getpass(prompt='Enter the keystore password: ')

    @staticmethod
    def optimizedJar(args):
        javaPkg, version, build = get_meta(args.package, args.endpoint)
        print(f"Executing {javaPkg}:build ...")
        result = str(bash(f"./gradlew {javaPkg}:build"))
        print(f"Executing {javaPkg}:optimizedJar ...")
        result = str(bash(f"./gradlew {javaPkg}:optimizedJar"))
        if not "BUILD SUCCESSFUL" in result:
            print(result)

    @staticmethod
    def deploy(args):
        print(f" -------------------- Deploying {args.package} ... -------------------- ")
        Command.optimizedJar(args)
        keystore_path = Command.get_keystore_path(args.endpoint)
        keystore_password = Command.get_keystore_password(args.endpoint)
        config = Config(args.endpoint, keystore_path, keystore_password)
        deploy(config, args.package)

    @staticmethod
    def update(args):
        print(f" -------------------- Updating {args.package} ... -------------------- ")
        Command.optimizedJar(args)
        keystore_path = Command.get_keystore_path(args.endpoint)
        keystore_password = Command.get_keystore_password(args.endpoint)
        config = Config(args.endpoint, keystore_path, keystore_password)
        update(config, args.package)

    @staticmethod
    def invoke(args):
        print(f" -------------------- Invoking {args.package} ... -------------------- ")
        keystore_path = Command.get_keystore_path(args.endpoint)
        keystore_password = Command.get_keystore_password(args.endpoint)
        config = Config(args.endpoint, keystore_path, keystore_password)
        invoke(config, args.package, args.params)

    @staticmethod
    def call(args):
        keystore_path = Command.get_keystore_path(args.endpoint)
        keystore_password = Command.get_keystore_password(args.endpoint)
        config = Config(args.endpoint, keystore_path, keystore_password)
        print(call(config, args.package, args.params))

if __name__ == "__main__":
    Command()
