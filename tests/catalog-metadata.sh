#!/usr/bin/env bash
set -euo pipefail
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
javac --release 17 -d "$tmp" reader/src/main/java/br/com/pythoteca/experimental/reader/catalog/CatalogMetadata.java tests/CatalogMetadataTest.java
java -cp "$tmp" CatalogMetadataTest
