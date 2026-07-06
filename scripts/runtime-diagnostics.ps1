function New-CpfRuntimeDiagnostic {
    param(
        [string] $Root,
        [string] $Module,
        [int[]] $Ports = @(),
        [string] $ErrorMessage = ""
    )

    function New-DiagnosticUnicodeText {
        param([int[]] $CodePoints)
        return -join ($CodePoints | ForEach-Object { [char] $_ })
    }
    $ClassificationEnvironment = New-DiagnosticUnicodeText @(0xD658, 0xACBD, 0x20, 0xBB38, 0xC81C)
    $ClassificationPermission = New-DiagnosticUnicodeText @(0xAD8C, 0xD55C, 0x20, 0xBB38, 0xC81C)
    $ClassificationDatabase = "DB " + (New-DiagnosticUnicodeText @(0xBB38, 0xC81C))
    $ClassificationImplementation = New-DiagnosticUnicodeText @(0xAD6C, 0xD604, 0x20, 0xBB38, 0xC81C)
    $ClassificationNeedsReview = New-DiagnosticUnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)

    if ([string]::IsNullOrWhiteSpace($Root)) {
        $Root = (Resolve-Path "$PSScriptRoot\..").Path
    }

    $portStates = @()
    foreach ($port in $Ports) {
        $client = [System.Net.Sockets.TcpClient]::new()
        $connected = $false
        try {
            $task = $client.ConnectAsync("127.0.0.1", $port)
            $connected = $task.Wait(700) -and $client.Connected
        } catch {
            $connected = $false
        } finally {
            $client.Dispose()
        }
        if (-not $connected) {
            $portStates += [ordered]@{
                port = $port
                state = "NOT_LISTENING"
                owningProcess = $null
            }
        } else {
            $portStates += [ordered]@{
                port = $port
                state = "LISTENING"
                owningProcess = $null
            }
        }
    }

    $classification = $ClassificationEnvironment
    if ($ErrorMessage -match "(?i)401|403|token|login|unauthorized|forbidden") {
        $classification = $ClassificationPermission
    } elseif ($ErrorMessage -match "(?i)SQL|JDBC|DataAccess|database|MariaDB|Hikari") {
        $classification = $ClassificationDatabase
    } elseif ($ErrorMessage -match "(?i)NullPointer|BeanCreation|NoSuchMethod|ClassNotFound|compile") {
        $classification = $ClassificationImplementation
    } elseif ($portStates.Count -gt 0 -and @($portStates | Where-Object { $_.state -ne "NOT_LISTENING" }).Count -gt 0) {
        $classification = $ClassificationNeedsReview
    }

    return [ordered]@{
        module = $Module
        classification = $classification
        error = $ErrorMessage
        ports = $portStates
        javaProcesses = @()
        processProbe = "skipped to keep runtime smoke bounded"
        lastAppLog = $null
        lastAppLogTail = @("skipped to keep runtime smoke bounded")
    }
}
