# canonical entrypoint 는 bin/cpf.ps1 하나다. 이 script 는 하위 호환 thin wrapper 이며
# 자체 명령 해석을 하지 않는다. 자체 해석을 넣으면 OS 사이 의미가 갈라진다.
& (Join-Path $PSScriptRoot 'cpf.ps1') reset @Args
exit $LASTEXITCODE
