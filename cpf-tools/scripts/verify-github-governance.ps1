param(
    [string]$Owner = "freeangelsun",
    [string]$Repository = "202412_01_CPF",
    [string]$Branch = "master",
    [string]$ResultDir = "build\cpf-results"
)
$ErrorActionPreference = "Stop"
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) { throw "GitHub CLI(gh)가 필요합니다." }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$stamp=(Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$evidence=Join-Path $ResultDir "github-governance-$stamp.json"
$protection = gh api "repos/$Owner/$Repository/branches/$Branch/protection" | ConvertFrom-Json
if (-not $protection.required_pull_request_reviews) { throw "Branch protection review rule missing." }
if ($protection.required_pull_request_reviews.required_approving_review_count -lt 1) { throw "At least one approving review is required." }
if (-not $protection.required_status_checks) { throw "Required status checks are missing." }
if ($protection.allow_force_pushes.enabled) { throw "Force push must be disabled." }
if ($protection.allow_deletions.enabled) { throw "Branch deletion must be disabled." }
[ordered]@{
  checkedAt=(Get-Date).ToUniversalTime().ToString("o")
  owner=$Owner; repository=$Repository; branch=$Branch
  approvingReviews=$protection.required_pull_request_reviews.required_approving_review_count
  dismissStaleReviews=$protection.required_pull_request_reviews.dismiss_stale_reviews
  requireCodeOwnerReviews=$protection.required_pull_request_reviews.require_code_owner_reviews
  strictStatusChecks=$protection.required_status_checks.strict
  statusChecks=@($protection.required_status_checks.contexts)
  forcePushAllowed=$protection.allow_force_pushes.enabled
  deletionAllowed=$protection.allow_deletions.enabled
} | ConvertTo-Json -Depth 8 | Set-Content -Encoding UTF8 $evidence
Write-Host "GitHub governance verification: PASS -> $evidence"
