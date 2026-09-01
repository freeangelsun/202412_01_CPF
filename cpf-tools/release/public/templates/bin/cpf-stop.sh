#!/usr/bin/env sh
# Thin wrapper. 실행 엔진은 cpf-cli.jar 이 단독 소유하며 이 파일은 인자 전달만 한다.
set -eu
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec "$DIR/cpf" runtime stop "$@"
