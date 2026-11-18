param([switch]$Configurar)
$ErrorActionPreference = 'Stop'
$directorio = Join-Path $env:LOCALAPPDATA 'HuellitasVet'
$archivo = Join-Path $directorio 'entorno.xml'
if ($Configurar -or (!(Test-Path $archivo) -and (!$env:DB_USER -or !$env:DB_PASSWORD -or !$env:JWT_SECRET))) {
    $usuario = Read-Host 'Usuario de MySQL'
    $clave = Read-Host 'Contraseña de MySQL' -AsSecureString
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    New-Item -ItemType Directory $directorio -Force | Out-Null
    [pscustomobject]@{
        Usuario = $usuario
        Clave = $clave
        Jwt = ConvertTo-SecureString ([Convert]::ToBase64String($bytes)) -AsPlainText -Force
    } | Export-Clixml -LiteralPath $archivo
}
if (Test-Path $archivo) {
    $local = Import-Clixml -LiteralPath $archivo
    if (!$env:DB_USER) { $env:DB_USER = $local.Usuario }
    if (!$env:DB_PASSWORD) { $env:DB_PASSWORD = [Net.NetworkCredential]::new('', $local.Clave).Password }
    if (!$env:JWT_SECRET) { $env:JWT_SECRET = [Net.NetworkCredential]::new('', $local.Jwt).Password }
}
Push-Location (Join-Path $PSScriptRoot '..')
try {
    & .\mvnw.cmd spring-boot:run
    $resultado = $LASTEXITCODE
} finally { Pop-Location }
exit $resultado
