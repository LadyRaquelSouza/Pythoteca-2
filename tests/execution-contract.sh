#!/usr/bin/env bash
set -euo pipefail
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
javac --release 17 -d "$tmp" protocol/src/main/java/br/com/pythoteca/protocol/ExecutionContract.java tests/ExecutionContractTest.java
java -cp "$tmp" ExecutionContractTest
