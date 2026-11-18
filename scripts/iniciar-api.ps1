param(
    [switch]$Configurar,
    [switch]$CrearAdministrador
)

$ErrorActionPreference = 'Stop'
$directorio = Join-Path $env:LOCALAPPDATA 'HuellitasVet'
$archivo = Join-Path $directorio 'entorno.xml'

# Las variables introducidas para el proceso de Maven se restauran al cerrar la API.
$anteriores = @{}
foreach ($nombre in 'DB_USER', 'DB_PASSWORD', 'JWT_SECRET', 'ADMIN_EMAIL', 'ADMIN_PASSWORD') {
    $anteriores[$nombre] = [Environment]::GetEnvironmentVariable($nombre, 'Process')
}

try {
    if ($Configurar -or (!(Test-Path -LiteralPath $archivo) -and (!$env:DB_USER -or !$env:DB_PASSWORD -or !$env:JWT_SECRET))) {
        $usuario = (Read-Host 'Usuario de MySQL de esta computadora').Trim()
        if (!$usuario) { throw 'Ingresa el nombre de usuario de MySQL.' }
        $clave = Read-Host 'Contraseña de MySQL' -AsSecureString
        $bytes = New-Object byte[] 32
        $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        New-Item -ItemType Directory $directorio -Force | Out-Null
        [pscustomobject]@{
            Usuario = $usuario
            Clave = $clave
            Jwt = [Net.NetworkCredential]::new('', [Convert]::ToBase64String($bytes)).SecurePassword
        } | Export-Clixml -LiteralPath $archivo
    }
    if (Test-Path -LiteralPath $archivo) {
        $local = Import-Clixml -LiteralPath $archivo
        if (!$env:DB_USER) { $env:DB_USER = $local.Usuario }
        if (!$env:DB_PASSWORD) { $env:DB_PASSWORD = [Net.NetworkCredential]::new('', $local.Clave).Password }
        if (!$env:JWT_SECRET) { $env:JWT_SECRET = [Net.NetworkCredential]::new('', $local.Jwt).Password }
    }

    $argumentosMaven = @('spring-boot:run')
    if ($CrearAdministrador) {
        $correo = (Read-Host 'Correo para iniciar sesión como primer ADMIN').Trim()
        if ($correo.Length -gt 100 -or $correo -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]+$') {
            throw 'Ingresa un correo válido de hasta 100 caracteres.'
        }
        $primera = Read-Host 'Nueva contraseña ADMIN (mínimo 10 caracteres, máximo 72 bytes)' -AsSecureString
        $segunda = Read-Host 'Repite la contraseña ADMIN' -AsSecureString
        try {
            $valor = [Net.NetworkCredential]::new('', $primera).Password
            if ([string]::IsNullOrWhiteSpace($valor) -or $valor.Length -lt 10 -or [Text.Encoding]::UTF8.GetByteCount($valor) -gt 72) {
                throw 'La contraseña ADMIN debe tener al menos 10 caracteres y ocupar hasta 72 bytes en UTF-8.'
            }
            if (![string]::Equals($valor, [Net.NetworkCredential]::new('', $segunda).Password, [StringComparison]::Ordinal)) { throw 'Las contraseñas ADMIN son diferentes.' }
            $env:ADMIN_EMAIL = $correo
            $env:ADMIN_PASSWORD = $valor
        } finally {
            $primera.Dispose()
            $segunda.Dispose()
            $valor = $null
        }
        $argumentosMaven += '-Dspring-boot.run.profiles=bootstrap'
        Write-Host 'Spring Boot creará el ADMIN únicamente si la tabla de usuarios está vacía.'
    }

    Push-Location (Join-Path $PSScriptRoot '..')
    try {
        & .\mvnw.cmd @argumentosMaven
        $resultado = $LASTEXITCODE
    } finally { Pop-Location }
} finally {
    foreach ($nombre in $anteriores.Keys) {
        [Environment]::SetEnvironmentVariable($nombre, $anteriores[$nombre], 'Process')
    }
}
exit $resultado
