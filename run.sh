#!/usr/bin/env bash

set -eu
cd "$(dirname "$0")"
exec java -javaagent:paperclip.jar -jar paperclip.jar "$@"
