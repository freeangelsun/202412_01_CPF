[CmdletBinding()] param([Parameter(Mandatory)][string]$CertificatePath,[int]$WarnDays=30)
$ErrorActionPreference='Stop'; $p=(Resolve-Path $CertificatePath).Path
$cert=[System.Security.Cryptography.X509Certificates.X509Certificate2]::new($p)
$now=(Get-Date).ToUniversalTime(); $remaining=[math]::Floor(($cert.NotAfter.ToUniversalTime()-$now).TotalDays)
$status=if($remaining -lt 0){'EXPIRED'}elseif($remaining -le $WarnDays){'WARN'}else{'OK'}
[ordered]@{subject=$cert.Subject;issuer=$cert.Issuer;serialNumber=$cert.SerialNumber;thumbprint=$cert.Thumbprint;notBefore=$cert.NotBefore.ToUniversalTime().ToString('o');notAfter=$cert.NotAfter.ToUniversalTime().ToString('o');remainingDays=$remaining;status=$status}|ConvertTo-Json
if($status -eq 'EXPIRED'){exit 2}; if($status -eq 'WARN'){exit 1}
