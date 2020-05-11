#!/bin/sh

set -e

execname=$(realpath $0)

echo "[Files]" > config
echo "MyBasedir = $(dirname $execname)" >> config
echo "GuavaJar = $(find $HOME/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/25.1-jre -name 'guava-25.1-jre.jar')" >> config
