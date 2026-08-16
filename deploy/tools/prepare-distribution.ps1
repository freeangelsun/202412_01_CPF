[CmdletBinding()] param(
  [ValidateSet('local','dev','stg','prod')][string]$Environment='dev',
  [ValidateSet('single-node','split-online','split-batch','full-distributed','custom')][string]$Topology='single-node',
  [string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,
  [switch]$PlanOnly
)
# Windows/Jenkins에서도 Linux와 동일한 Python 정본 Packager를 호출합니다.
$args=@((Join-Path $Root 'deploy\tools\prepare-distribution.py'),'--root',$Root,'--env',$Environment,'--topology',$Topology)
if($PlanOnly){$args+='--plan-only'}
python @args
