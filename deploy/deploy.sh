#!/bin/bash

# Deployment script for Club Portal
# Usage: chmod +x deploy.sh && ./deploy.sh

set -e  # Exit immediately if any command fails

echo "========================================="
echo "  Club Portal 部署脚本"
echo "========================================="

# Configuration
FRONTEND_DIR="/var/www/club-portal"
BACKEND_DIR="/opt/club-portal-backend"
BACKEND_JAR="club-portal-backend-1.0-SNAPSHOT.jar"
NGINX_CONF="/etc/nginx/sites-available/club-portal"
NGINX_ENABLED="/etc/nginx/sites-enabled/club-portal"

# 1. Update system packages
echo ""
echo "步骤 1: 更新系统包..."
sudo apt-get update

# 2. Install required software
echo ""
echo "步骤 2: 安装必要软件..."
sudo apt-get install -y nginx openjdk-17-jdk

# 3. Create deployment directories
echo ""
echo "步骤 3: 创建部署目录..."
sudo mkdir -p $FRONTEND_DIR
sudo mkdir -p $BACKEND_DIR

# 4. Deploy frontend assets
echo ""
echo "步骤 4: 部署前端静态文件..."
if [ -d "dist" ]; then
    echo "Using built frontend output from dist/"
    sudo cp -r dist/* "$FRONTEND_DIR/"
elif [ -d "frontend" ]; then
    echo "Warning: dist/ not found. Falling back to frontend/ source files."
    sudo cp -r frontend/* "$FRONTEND_DIR/"
else
    echo "Error: missing dist/ (preferred) and frontend/ (legacy fallback)"
    exit 1
fi
sudo chown -R www-data:www-data $FRONTEND_DIR
sudo chmod -R 755 $FRONTEND_DIR
echo "前端文件已复制到: $FRONTEND_DIR"

# 5. Deploy backend
echo ""
echo "步骤 5: 部署后端JAR文件..."
JAR_SRC=""
if [ -f "backend/target/$BACKEND_JAR" ]; then
    JAR_SRC="backend/target/$BACKEND_JAR"
elif [ -f "backend.jar" ]; then
    JAR_SRC="backend.jar"
elif [ -f "$BACKEND_JAR" ]; then
    JAR_SRC="$BACKEND_JAR"
fi

if [ -z "$JAR_SRC" ]; then
    echo "错误: 未找到后端JAR文件。请先运行 mvn clean package，或上传 backend.jar 到当前目录。"
    exit 1
fi

sudo cp "$JAR_SRC" "$BACKEND_DIR/$BACKEND_JAR"
echo "后端JAR已复制到: $BACKEND_DIR/$BACKEND_JAR"

# 6. Configure Nginx
echo ""
echo "步骤 6: 配置Nginx..."
NGINX_SRC=""
if [ -f "deploy/nginx.conf" ]; then
    NGINX_SRC="deploy/nginx.conf"
elif [ -f "nginx.conf" ]; then
    NGINX_SRC="nginx.conf"
fi

if [ -z "$NGINX_SRC" ]; then
    echo "错误: 未找到 nginx.conf（deploy/nginx.conf 或 nginx.conf）。"
    exit 1
fi

sudo cp "$NGINX_SRC" "$NGINX_CONF"

# Create the site symlink
if [ ! -L $NGINX_ENABLED ]; then
    sudo ln -s $NGINX_CONF $NGINX_ENABLED
fi

# Remove the default site configuration
sudo rm -f /etc/nginx/sites-enabled/default

# Test the Nginx configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
sudo systemctl enable nginx
echo "Nginx已配置并重启"

# 7. Create the backend systemd service
echo ""
echo "步骤 7: 配置后端服务..."
sudo tee /etc/systemd/system/club-portal-backend.service > /dev/null <<EOF
[Unit]
Description=Club Portal Backend Service
After=syslog.target network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=$BACKEND_DIR
EnvironmentFile=-/etc/club-portal-backend.env
ExecStart=/usr/bin/java -jar $BACKEND_DIR/$BACKEND_JAR
Restart=always
RestartSec=10
StandardOutput=syslog
StandardError=syslog
SyslogIdentifier=club-portal

[Install]
WantedBy=multi-user.target
EOF

# 8. Start the backend service
echo ""
echo "步骤 8: 启动后端服务..."
sudo systemctl daemon-reload
sudo systemctl enable club-portal-backend
sudo systemctl restart club-portal-backend

# Wait for the service to start
sleep 5

# Check the service status
if sudo systemctl is-active --quiet club-portal-backend; then
    echo "✅ 后端服务启动成功"
else
    echo "❌ 后端服务启动失败，查看日志:"
    sudo journalctl -u club-portal-backend -n 50
    exit 1
fi

# 9. Configure the firewall
echo ""
echo "步骤 9: 配置防火墙..."
sudo ufw allow 'Nginx Full'
sudo ufw allow 22
sudo ufw --force enable

# 10. Show deployment details
echo ""
echo "========================================="
echo "  部署完成！"
echo "========================================="
echo ""
echo "服务状态:"
echo "  Nginx: $(sudo systemctl is-active nginx)"
echo "  后端:  $(sudo systemctl is-active club-portal-backend)"
echo ""
echo "访问地址:"
echo "  HTTP:  http://$(curl -s ifconfig.me)"
echo "  本地:  http://localhost"
echo ""
echo "有用的命令:"
echo "  查看后端日志: sudo journalctl -u club-portal-backend -f"
echo "  重启后端:     sudo systemctl restart club-portal-backend"
echo "  重启Nginx:    sudo systemctl restart nginx"
echo "  查看Nginx日志: sudo tail -f /var/log/nginx/error.log"
echo ""
echo "下一步:"
echo "  1. 将域名DNS解析指向服务器IP"
echo "  2. 修改nginx.conf中的server_name为你的域名"
echo "  3. 使用Let's Encrypt配置HTTPS (可选)"
echo ""
