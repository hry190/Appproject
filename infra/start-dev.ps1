# 启动完整 dev 环境(后端 + 数据库 + 缓存 + 对象存储 + 杀毒扫描 + worker)
# 跑完等 ~30 秒,API 健康:curl http://127.0.0.1:8010/docs 看到 Swagger UI
docker compose -f docker-compose.yml `
                 -f docker-compose.override.yml `
                 up -d
Write-Host ""
Write-Host "✓ 所有服务已启动(首次启动会下载镜像并运行 migrations,约 1-3 分钟)"
Write-Host ""
Write-Host "API 地址:http://127.0.0.1:8010"
Write-Host "Postgres: localhost:55432"
Write-Host "Redis:    localhost:16379"
Write-Host "MinIO:    localhost:19000(S3)/19001(console)"
Write-Host ""
Write-Host "看日志:docker compose -f docker-compose.yml -f docker-compose.override.yml logs -f api"
