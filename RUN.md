运行说明（可直接复制）

前置条件

本地已安装 Java（匹配你的 Spring Boot 项目版本）

能 SSH 登录 EC2（pem：C:\Users\shenn\Sntc00715.pem）

EC2 能访问 RDS（已验证）

RDS 数据库：club_portal

本地 Spring Boot 通过 SSH 隧道访问 RDS（不需要 RDS 公网开放）

建立 SSH 隧道（本地 Windows PowerShell）
说明：此隧道将本地 127.0.0.1:3307 转发到 RDS 3306，经由 EC2 跳板。

逐行执行（每行单独回车），并保持窗口不要关闭：

ssh -i "C:\Users\shenn\Sntc00715.pem" -N -L 3307:club-portal-db.cfaws8eo43v2.eu-west-2.rds.amazonaws.com:3306 ec2-user@3.8.149.144

后端配置（Spring Boot）
在 application.yml（或 properties）配置数据源指向隧道：

spring.datasource.url=jdbc:mysql://127.0.0.1:3307/club_portal?useSSL=false&serverTimezone=UTC

spring.datasource.username=stinc

spring.datasource.password=<RDS密码>

spring.jpa.hibernate.ddl-auto=validate（避免自动改表）

管理员写接口保护（仅 /api/admin/**）：

app.adminToken=change-me-123
调用 admin 写接口时需 header：X-ADMIN-TOKEN: change-me-123

启动后端

启动 Spring Boot（IDE 或命令行均可）

验证：打开 http://localhost:8080/debug/db

db 应为 club_portal

host 应类似 ip-10-...（说明已连到 RDS，而不是本地 MySQL）

初始化演示数据（seed.ps1）

运行 seed（你的 PowerShell 脚本）

记录输出 IDs（示例）：

clubId=2

venueId=2

planId=2

userMembershipId=2

启动前端（纯 HTML/CSS/JS）

打开 index.html（或使用 Live Server）

默认输入：

userId=1

userMembershipId=2

时间范围：2026-01-16T00:00:00 → 2026-01-17T00:00:00

演示流程（端到端）

点击 Clubs：选择 Basketball Club（seed 创建的）

Timeslots 显示：同时包含 non-members 与 members-only 时段

点击 Book：

对 members-only 时段，带 userMembershipId=2 可成功预约并使用会员价 membersPrice=3.00

若清空 userMembershipId 再点 members-only，会返回 400：Members-only timeslot requires userMembershipId

My Bookings：

自动加载 GET /api/users/1/bookings

对 status=0 的 booking 点击 Cancel：调用 POST /api/bookings/{id}/cancel，状态变 2 且出现 cancelTime

核心接口清单（用于验收）
读接口：

GET /api/clubs（ClubDto）

GET /api/clubs/{clubId}/timeslots?from=...&to=...（TimeSlotDto）

GET /api/users/{userId}/bookings（BookingDto[]）

用户写接口：

POST /api/bookings（BookingDto）

POST /api/bookings/{id}/cancel（BookingDto）

POST /api/user-memberships/purchase（返回 userMembershipId）

管理员写接口（需 X-ADMIN-TOKEN）：

POST /api/admin/clubs

POST /api/admin/venues

POST /api/admin/timeslots

POST /api/admin/membership-plans
