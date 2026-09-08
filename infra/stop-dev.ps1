# 停 dev 环境(保留数据卷,下次启动数据还在)
docker compose -f docker-compose.yml `
                 -f docker-compose.override.yml `
                 down
Write-Host "✓ 容器已停(数据卷保留,重新 start-dev 还能复用)"
