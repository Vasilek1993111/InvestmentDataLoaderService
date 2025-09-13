# PowerShell script for building and running Docker container with external DB
# Usage: .\docker-build-external-db.ps1 [build|start|stop|restart|logs]

param(
    [string]$Action = "start"
)

# Functions for output messages
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Check Docker availability
function Test-Docker {
    try {
        docker --version | Out-Null
        docker-compose --version | Out-Null
        return $true
    }
    catch {
        Write-Error "Docker or Docker Compose not installed. Please install them."
        return $false
    }
}

# Check environment variables
function Test-Environment {
    if (-not (Test-Path ".env")) {
        Write-Warning "File .env not found. Creating it..."
        $envContent = @"
# Tinkoff Invest API token
T_INVEST_TOKEN=your_actual_tinkoff_invest_token_here
"@
        $envContent | Out-File -FilePath ".env" -Encoding UTF8
        Write-Warning "Created .env file. Please edit it and specify real token."
    }
}

# Build image
function Build-Image {
    Write-Info "Building Docker image for external DB..."
    docker-compose -f docker-compose.external-db.yml build --no-cache
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Image built successfully!"
    } else {
        Write-Error "Error building image"
        exit 1
    }
}

# Start containers
function Start-Containers {
    Write-Info "Starting application with external DB connection..."
    docker-compose -f docker-compose.external-db.yml up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Application started!"
        Write-Info "Application available at: http://localhost:8083"
        Write-Info "Health Check: http://localhost:8083/actuator/health"
        Write-Warning "Make sure your external DB is running and configured correctly!"
    } else {
        Write-Error "Error starting application"
        exit 1
    }
}

# Stop containers
function Stop-Containers {
    Write-Info "Stopping application..."
    docker-compose -f docker-compose.external-db.yml down
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Application stopped!"
    } else {
        Write-Error "Error stopping application"
        exit 1
    }
}

# Restart containers
function Restart-Containers {
    Write-Info "Restarting application..."
    docker-compose -f docker-compose.external-db.yml restart
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Application restarted!"
    } else {
        Write-Error "Error restarting application"
        exit 1
    }
}

# View logs
function Show-Logs {
    Write-Info "Showing application logs..."
    docker-compose -f docker-compose.external-db.yml logs -f
}

# Cleanup
function Invoke-Cleanup {
    Write-Info "Cleaning up unused Docker resources..."
    docker system prune -f
    if ($LASTEXITCODE -eq 0) {
        Write-Info "Cleanup completed!"
    } else {
        Write-Error "Error during cleanup"
        exit 1
    }
}

# Test database connection
function Test-DatabaseConnection {
    Write-Info "Testing DB connection..."
    $logs = docker logs investment-data-loader-service 2>&1
    if ($logs -match "Started InvestmentDataLoaderServiceApplication") {
        Write-Info "Application started successfully!"
    } elseif ($logs -match "Connection refused|Connection timed out|Database connection failed") {
        Write-Error "DB connection error. Check settings in .env file."
    } else {
        Write-Warning "Could not determine DB connection status. Check logs."
    }
}

# Main logic
function Main {
    if (-not (Test-Docker)) {
        exit 1
    }
    
    Test-Environment
    
    switch ($Action.ToLower()) {
        "build" {
            Build-Image
        }
        "start" {
            Build-Image
            Start-Containers
            Start-Sleep -Seconds 10
            Test-DatabaseConnection
        }
        "stop" {
            Stop-Containers
        }
        "restart" {
            Restart-Containers
            Start-Sleep -Seconds 10
            Test-DatabaseConnection
        }
        "logs" {
            Show-Logs
        }
        "cleanup" {
            Invoke-Cleanup
        }
        "test-db" {
            Test-DatabaseConnection
        }
        default {
            Write-Host "Usage: .\docker-build-external-db.ps1 [build|start|stop|restart|logs|cleanup|test-db]"
            Write-Host ""
            Write-Host "Commands:"
            Write-Host "  build     - Build Docker image"
            Write-Host "  start     - Build and start application (default)"
            Write-Host "  stop      - Stop application"
            Write-Host "  restart   - Restart application"
            Write-Host "  logs      - Show logs"
            Write-Host "  cleanup   - Clean up unused Docker resources"
            Write-Host "  test-db   - Test DB connection"
            Write-Host ""
            Write-Host "IMPORTANT: Make sure your external DB is running and accessible!"
            exit 1
        }
    }
}

Main