$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080"
$runId = [guid]::NewGuid().ToString("N")
$emailA = "taskflow-u6-a-$runId@example.test"
$emailB = "taskflow-u6-b-$runId@example.test"
$password = "U6-$runId-TaskFlow!"
$tokenA = $null
$tokenB = $null
$jtiA = $null
$jtiB = $null
$dbContainerId = $null
$dbUser = $null
$dbName = $null
$dbCountsBefore = $null

Add-Type -AssemblyName System.Net.Http

function Write-Pass([string] $message) {
  Write-Output "PASS: $message"
}

function Assert-Status($response, [int] $expected, [string] $description) {
  if ($response.Status -ne $expected) {
    throw "$description expected HTTP $expected, got HTTP $($response.Status)."
  }
  Write-Pass "$description (HTTP $expected)"
}

function Invoke-Api(
  [string] $method,
  [string] $path,
  [string] $token = $null,
  $body = $null
) {
  $request = [System.Net.Http.HttpRequestMessage]::new(
    [System.Net.Http.HttpMethod]::new($method),
    "$baseUrl$path"
  )
  if ($token) {
    $request.Headers.Authorization =
      [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $token)
  }
  if ($null -ne $body) {
    $json = ConvertTo-Json -InputObject $body -Depth 8 -Compress
    $request.Content = [System.Net.Http.StringContent]::new(
      $json,
      [System.Text.Encoding]::UTF8,
      "application/json"
    )
  }

  $response = $null
  try {
    $response = $httpClient.SendAsync($request).GetAwaiter().GetResult()
    $content = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    $data = $null
    if ($content) {
      try { $data = ConvertFrom-Json -InputObject $content } catch { $data = $content }
    }
    return [pscustomobject]@{ Status = [int]$response.StatusCode; Data = $data }
  } finally {
    $request.Dispose()
    if ($response) { $response.Dispose() }
  }
}

function Read-Jti([string] $token) {
  $payload = $token.Split(".")[1].Replace("-", "+").Replace("_", "/")
  switch ($payload.Length % 4) {
    2 { $payload += "==" }
    3 { $payload += "=" }
  }
  $json = [System.Text.Encoding]::UTF8.GetString(
    [System.Convert]::FromBase64String($payload)
  )
  return (ConvertFrom-Json -InputObject $json).jti
}

function Get-ComposeContainerId([string] $service) {
  $ids = @(
    & docker ps --filter "label=com.docker.compose.service=$service" --format "{{.ID}}"
  )
  if ($LASTEXITCODE -ne 0 -or $ids.Count -ne 1) {
    throw "Expected exactly one running Compose '$service' container."
  }
  return $ids[0].Trim()
}

function Invoke-DatabaseQuery([string] $query) {
  $output = $query | & docker exec -i $dbContainerId psql `
    -v ON_ERROR_STOP=1 -U $dbUser -d $dbName -tA -f - 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "Temporary UAT data cleanup/count query failed."
  }
  return ($output | Out-String).Trim()
}

function Get-DatabaseCounts {
  return Invoke-DatabaseQuery `
    "SELECT (SELECT COUNT(*) FROM users)||','||(SELECT COUNT(*) FROM tasks)||','||(SELECT COUNT(*) FROM revoked_tokens);"
}

$handler = [System.Net.Http.HttpClientHandler]::new()
$handler.UseProxy = $false
$httpClient = [System.Net.Http.HttpClient]::new($handler)
$httpClient.Timeout = [TimeSpan]::FromSeconds(15)

try {
  $backendId = Get-ComposeContainerId "backend"
  $dbContainerId = Get-ComposeContainerId "db"
  $frontendId = Get-ComposeContainerId "frontend"
  $dbEnvironment = ConvertFrom-Json (
    & docker inspect --format '{{json .Config.Env}}' $dbContainerId
  )
  $dbUser = ($dbEnvironment | Where-Object { $_ -like "POSTGRES_USER=*" } |
    Select-Object -First 1).Substring("POSTGRES_USER=".Length)
  $dbName = ($dbEnvironment | Where-Object { $_ -like "POSTGRES_DB=*" } |
    Select-Object -First 1).Substring("POSTGRES_DB=".Length)
  if (-not $dbUser -or -not $dbName) {
    throw "Could not determine the existing PostgreSQL database and user settings."
  }
  $backendLabels = ConvertFrom-Json (
    & docker inspect --format '{{json .Config.Labels}}' $backendId
  )
  $project = $backendLabels.PSObject.Properties["com.docker.compose.project"].Value
  foreach ($id in @($dbContainerId, $frontendId)) {
    $otherLabels = ConvertFrom-Json (
      & docker inspect --format '{{json .Config.Labels}}' $id
    )
    $otherProject = $otherLabels.PSObject.Properties["com.docker.compose.project"].Value
    if ($otherProject -ne $project) {
      throw "Compose services do not belong to the same project."
    }
  }

  $dbMounts = ConvertFrom-Json (
    & docker inspect --format '{{json .Mounts}}' $dbContainerId
  )
  $volumeName = ($dbMounts | Where-Object {
    $_.Destination -eq "/var/lib/postgresql/data"
  } | Select-Object -First 1).Name
  if ($volumeName -ne "taskflow-postgres-data") {
    throw "Expected PostgreSQL named volume taskflow-postgres-data, found '$volumeName'."
  }
  $dbCountsBefore = Get-DatabaseCounts
  Write-Pass "Compose frontend/backend/database are running; PostgreSQL uses the existing named volume"
  Write-Pass "Pre-test database row counts captured without changing existing records"

  $registration = @{ email = $emailA; password = $password }
  Assert-Status (Invoke-Api "POST" "/api/auth/register" $null $registration) 201 "Register user A"
  $duplicate = Invoke-Api "POST" "/api/auth/register" $null $registration
  Assert-Status $duplicate 409 "Reject duplicate registration"
  Assert-Status (Invoke-Api "POST" "/api/auth/login" $null @{
      email = $emailA
      password = "invalid-$runId"
    }) 401 "Reject invalid login"

  $loginA = Invoke-Api "POST" "/api/auth/login" $null $registration
  Assert-Status $loginA 200 "Login user A"
  $tokenA = $loginA.Data.accessToken
  $jtiA = Read-Jti $tokenA

  $registrationB = @{ email = $emailB; password = $password }
  Assert-Status (Invoke-Api "POST" "/api/auth/register" $null $registrationB) 201 "Register independent user B"
  $loginB = Invoke-Api "POST" "/api/auth/login" $null $registrationB
  Assert-Status $loginB 200 "Login user B"
  $tokenB = $loginB.Data.accessToken
  $jtiB = Read-Jti $tokenB

  $create = Invoke-Api "POST" "/api/tasks" $tokenA @{
    title = "U6 isolated acceptance task"
    description = "Temporary task used by the U6 Compose smoke test"
  }
  Assert-Status $create 201 "Create user A task"
  $taskId = $create.Data.id
  $listA = Invoke-Api "GET" "/api/tasks" $tokenA
  Assert-Status $listA 200 "List user A tasks"
  if (@($listA.Data | Where-Object { $_.id -eq $taskId }).Count -ne 1) {
    throw "User A task was not present in user A's list."
  }
  Assert-Status (Invoke-Api "GET" "/api/tasks/$taskId" $tokenA) 200 "Read user A task"
  $patchA = Invoke-Api "PATCH" "/api/tasks/$taskId" $tokenA @{
    title = "U6 isolated acceptance task updated"
    completed = $true
  }
  Assert-Status $patchA 200 "Update user A task"

  $listB = Invoke-Api "GET" "/api/tasks" $tokenB
  Assert-Status $listB 200 "List user B tasks"
  if (@($listB.Data | Where-Object { $_.id -eq $taskId }).Count -ne 0) {
    throw "User B's task list exposed user A's task."
  }
  Assert-Status (Invoke-Api "GET" "/api/tasks/$taskId" $tokenB) 404 "Deny user B task read"
  Assert-Status (Invoke-Api "PATCH" "/api/tasks/$taskId" $tokenB @{
      title = "Unauthorized update"
    }) 404 "Deny user B task update"
  Assert-Status (Invoke-Api "DELETE" "/api/tasks/$taskId" $tokenB) 404 "Deny user B task deletion"
  $readAfterDenials = Invoke-Api "GET" "/api/tasks/$taskId" $tokenA
  if ($readAfterDenials.Data.title -ne "U6 isolated acceptance task updated") {
    throw "User B's denied update changed user A's task."
  }
  Write-Pass "Cross-user denials did not alter user A's task"

  Assert-Status (Invoke-Api "DELETE" "/api/tasks/$taskId" $tokenA) 204 "Delete user A task"
  Assert-Status (Invoke-Api "GET" "/api/tasks/$taskId" $tokenA) 404 "Confirm deleted task is absent"

  Assert-Status (Invoke-Api "POST" "/api/auth/logout" $tokenA) 204 "Logout user A"

  $restartOutput = & docker restart $backendId 2>&1
  if ($LASTEXITCODE -ne 0) {
    throw "Restarting only the backend container failed."
  }
  Write-Pass "Restarted only the backend container"

  $ready = $false
  for ($attempt = 0; $attempt -lt 60; $attempt++) {
    try {
      $health = Invoke-Api "GET" "/api/health"
      if ($health.Status -eq 200 -and $health.Data.status -eq "UP") {
        $ready = $true
        break
      }
    } catch {}
    Start-Sleep -Seconds 2
  }
  if (-not $ready) { throw "Backend did not become healthy after restart." }
  Write-Pass "Backend became healthy after restart"

  if ((Get-ComposeContainerId "db") -ne $dbContainerId) {
    throw "Database container changed during backend-only restart."
  }
  if ((Get-ComposeContainerId "frontend") -ne $frontendId) {
    throw "Frontend container changed during backend-only restart."
  }
  $mountsAfterRestart = ConvertFrom-Json (
    & docker inspect --format '{{json .Mounts}}' $dbContainerId
  )
  $volumeAfterRestart = ($mountsAfterRestart | Where-Object {
    $_.Destination -eq "/var/lib/postgresql/data"
  } | Select-Object -First 1).Name
  if ($volumeAfterRestart -ne $volumeName) {
    throw "PostgreSQL volume changed during backend-only restart."
  }
  Write-Pass "Database/frontend containers and PostgreSQL volume remained unchanged"

  Assert-Status (Invoke-Api "GET" "/api/tasks" $tokenA) 401 "Reject revoked user A token after backend restart"
  Assert-Status (Invoke-Api "GET" "/api/tasks" $tokenB) 200 "Accept non-revoked user B token after backend restart"
  Assert-Status (Invoke-Api "POST" "/api/auth/logout" $tokenB) 204 "Logout user B"

  Write-Output "U6 Compose black-box smoke test passed."
} finally {
  if ($dbContainerId -and $emailA -and $emailB) {
    $safeEmailA = $emailA.Replace("'", "''")
    $safeEmailB = $emailB.Replace("'", "''")
    $jtiClause = @()
    if ($jtiA) { $jtiClause += "'" + $jtiA.Replace("'", "''") + "'" }
    if ($jtiB) { $jtiClause += "'" + $jtiB.Replace("'", "''") + "'" }
    $cleanup = @"
BEGIN;
DELETE FROM tasks WHERE owner_id IN (SELECT id FROM users WHERE email IN ('$safeEmailA', '$safeEmailB'));
$(if ($jtiClause.Count -gt 0) { "DELETE FROM revoked_tokens WHERE jti IN ($($jtiClause -join ', '));" })
DELETE FROM users WHERE email IN ('$safeEmailA', '$safeEmailB');
COMMIT;
"@
    try {
      Invoke-DatabaseQuery $cleanup | Out-Null
      if ($dbCountsBefore) {
        $countsAfter = Get-DatabaseCounts
        if ($countsAfter -ne $dbCountsBefore) {
          Write-Warning "UAT cleanup completed, but database table row counts differ from the captured baseline."
        } else {
          Write-Pass "Only the temporary UAT users, task, and their revoked tokens were removed; row counts returned to baseline"
        }
      }
    } catch {
      Write-Warning "Could not clean up only the temporary UAT records. Inspect the database before rerunning."
      throw
    }
  }
  if ($httpClient) { $httpClient.Dispose() }
  if ($handler) { $handler.Dispose() }
}
