# Google Sign-In Setup Guide

本文档说明如何在 Google Cloud Console 中配置 OAuth 2.0 Client，以使 stinc2025 前端能够集成 Google Sign-In。

## 当前问题：错误 400 - invalid_request

**错误信息**：  
"禁止访问：发生了授权错误" / "You can't sign in to this app because it doesn't comply with Google's OAuth 2.0 policy"

**状态**：
- ✅ Authorized JavaScript origins 已添加（http://127.0.0.1:5500, http://localhost:5500）
- ⚠️ **需要添加 Authorized redirect URIs**
- ⚠️ **需要检查 OAuth consent screen 配置**

---

## 解决方案：添加 Redirect URIs

即使使用 popup 模式，Google Sign-In 仍然需要配置 redirect URIs。

---

## 步骤 1：获取你的本地 Origin

当你用 VS Code Live Server 打开页面时，浏览器地址栏显示的 origin 是：
```
http://127.0.0.1:5500
```

**注意**：
- 如果你用 `localhost` 访问（http://localhost:5500），那就用 `http://localhost:5500`。
- 本指南以 `http://127.0.0.1:5500` 为例。

---

## 步骤 2：在 Google Cloud Console 中添加 Authorized JavaScript Origins

### 2.1 打开 Google Cloud Console

访问 [Google Cloud Console](https://console.cloud.google.com/)，使用你创建 OAuth 2.0 Client 时的 Google 账号登录。

### 2.2 选择项目

确保你在正确的项目中（左上角下拉菜单）。

### 2.3 导航到 Credentials

左侧菜单：**APIs & Services** → **Credentials**

### 2.4 找到并编辑 OAuth 2.0 Client ID

在 "OAuth 2.0 Client IDs" 表中，找到你的 Web Application 客户端（类型显示为 "Web application"），点击其名称或点击右侧的编辑按钮。

**Client ID 应该是** `885940597719-mtcoo82k9bbksvuj786nsj7iombqtsli.apps.googleusercontent.com`

### 2.5 在 Authorized JavaScript origins 中添加你的 Origin（已完成✅）

在打开的编辑页面中，找到 **Authorized JavaScript origins** 部分。

已添加：
```
http://127.0.0.1:5500
http://localhost:5500
```

### 2.6 ⚠️ 添加 Authorized redirect URIs（关键步骤）

在同一个编辑页面中，找到 **Authorized redirect URIs** 部分。

点击 **+ Add URI** 按钮，**逐个添加**以下 URI：

```
http://127.0.0.1:5500
http://127.0.0.1:5500/login.html
http://localhost:5500
http://localhost:5500/login.html
```

**为什么需要？** 即使使用 popup 模式，Google Sign-In 也需要 redirect URIs 作为安全回调地址。

### 2.7 检查 OAuth consent screen

在左侧菜单点击 **OAuth consent screen**：

1. **User Type**：如果是测试，确保选择了 "External" 并添加测试用户
2. **Test users**：点击 "ADD USERS"，添加你用来测试的 Google 账号邮箱
3. **Scopes**：确保包含基本权限（userinfo.email, userinfo.profile, openid）

### 2.8 保存更改并等待

点击 **保存** (Save)，然后**等待 5-10 分钟**让配置生效。

---

## 步骤 3：启动本地后端服务

打开新的终端窗口，进入仓库的 `server` 目录：

```powershell
cd c:\Users\shenn\Desktop\stinc2025\server
node server.js
```

你应该看到输出：
```
Mock backend listening on http://localhost:8080
```

> ⚠️ **务必通过 HTTP 打开前端**：不要直接双击 `login.html` 走 `file://` 协议，因为浏览器会发出 `Origin: null` 的跨域请求，后端会拒绝。请使用 VS Code Live Server 或运行 `npx serve .` / `python -m http.server 5500` 等方式，让地址栏是 `http://127.0.0.1:<port>`。

---

## 步骤 4：在本地浏览器测试 Google Sign-In

### 4.1 打开 VS Code Live Server

在 VS Code 中打开 `login.html`，点击右键 → "Open with Live Server"。

浏览器应该自动打开 `http://127.0.0.1:5500/login.html`（或类似的 URL）。

### 4.2 打开浏览器开发者工具

按 **F12** 打开开发者工具，切换到 **Console** 标签。

### 4.3 点击 "Sign in with Google" 按钮

页面上应该有 Google Sign-In 按钮（蓝色标准谷歌登录按钮），点击它。

### 4.4 预期的流程

1. Google 登录弹窗会出现（如果没有弹窗，检查浏览器是否阻止了弹窗）。
2. 用你的 Google 账号登录（或选择已有的账号）。
3. 如果这是第一次，Google 可能要求你授权应用访问你的信息。
4. 授权后，浏览器会跳转到 `home.html`（表示登录成功）。
5. 在浏览器 Console 中，你应该看到：
   ```
   [GSI CALLBACK] response: {credential: "eyJ..."}
   [GSI CALLBACK] idToken (first 32 chars): eyJhbGciOiJSUzI1NiIsInR5cCI...
   [mock-backend] /api/auth/google -> your.email@gmail.com
   ```

### 4.5 如果失败

#### 问题 1：仍然看到 "The given origin is not allowed"

- 确保你在 Google Cloud Console 中添加的 origin 与浏览器地址栏显示的 origin **完全相同**。
- 清除浏览器缓存（Ctrl+Shift+Delete），然后刷新页面（Ctrl+F5）。
- 等待几分钟，Google Cloud 配置可能需要一些时间生效。

#### 问题 2：弹窗被浏览器阻止

- 检查浏览器地址栏右侧是否有弹窗通知，点击 "总是允许" (Always allow)。
- 或者在浏览器设置中，允许 127.0.0.1:5500 的弹窗。

#### 问题 3：登录后没有跳转到 home.html

- 查看浏览器 Console 中是否有错误消息。
- 确认后端服务 (http://localhost:8080) 正在运行。
- 如果 Console 显示 "TypeError: Failed to fetch"，可能是 CORS 问题；检查后端是否正确配置了 CORS。

#### 问题 4：COOP/COEP 警告

如果你在 Console 中看到：
```
Uncaught DOMException: Blocked a frame with origin "..." from accessing a cross-origin frame.
Cross-Origin-Opener-Policy: ...
```

这表示 Google Sign-In 的弹窗被 COOP 政策阻止了。解决办法：

**选项 A**：在后端返回以下 HTTP 响应头（如果你有 Spring Boot 后端）：
```
Cross-Origin-Opener-Policy: same-origin-allow-popups
```

**选项 B**：使用 VS Code 的 Live Server 扩展的设置，添加自定义响应头（通常需要修改扩展配置）。

**选项 C**：改用 `data-ux_mode="redirect"` 而不是 `popup`（在 `login.html` 的 `g_id_onload` 标签中），这样 Google 登录会在同一个标签页中进行，而不是弹窗。

---

## 本地测试的完整命令流

如果你想一步步手动测试，使用以下命令：

### 在终端 1 中启动后端：
```powershell
cd c:\Users\shenn\Desktop\stinc2025\server
node server.js
```

### 在终端 2 中（可选，如果使用 python http.server）：
```powershell
cd c:\Users\shenn\Desktop\stinc2025
python -m http.server 5500
```

**注意**：VS Code Live Server 通常会在端口 5500 上自动启动，所以通常你不需要手动启动。

### 在浏览器中：
1. 打开 `http://127.0.0.1:5500/login.html`（或 Live Server 提供的 URL）
2. 按 F12 打开 Console
3. 点击 "Sign in with Google"
4. 授权登录
5. 查看 Console 输出和浏览器是否跳转到 `home.html`

---

## 后端代码参考

### Mock 后端（Express.js）

位置：`server/server.js`

```javascript
const express = require('express');
const cors = require('cors');
const app = express();
const port = 8080;

app.use(cors());
app.use(express.json());

app.post('/api/auth/google', (req, res) => {
  const { credential } = req.body || {};
  if (!credential) return res.status(400).json({ error: 'missing credential' });

  // 解析 JWT 以提取 email/name（不验证签名）
  let payload = null;
  try {
    const parts = credential.split('.');
    if (parts.length >= 2) {
      const b = Buffer.from(parts[1].replace(/-/g,'+').replace(/_/g,'/'), 'base64');
      payload = JSON.parse(b.toString('utf8'));
    }
  } catch (e) {
    payload = null;
  }

  const email = (payload && (payload.email || payload.sub)) || 'user@example.com';
  const fullName = (payload && payload.name) || (email.split('@')[0]);

  const user = {
    id: payload && payload.sub ? payload.sub : 'local-mock-' + Math.floor(Math.random()*100000),
    fullName,
    email,
    role: 'USER'
  };

  console.log('[mock-backend] /api/auth/google ->', user.email);
  return res.json(user);
});

app.listen(port, () => console.log(`Mock backend listening on http://localhost:${port}`));
```

### Spring Boot 后端（参考实现）

如果你的后端是 Spring Boot，可以参考以下控制器和 CORS 配置：

#### 1. OAuth2 控制器

```java
package com.stinc2025.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.JsonFactory;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Value("${google.oauth2.client-id:885940597719-mtcoo82k9bbksvuj786nsj7iombqtsli.apps.googleusercontent.com}")
    private String googleClientId;

    @PostMapping("/auth/google")
    public ResponseEntity<?> authenticateGoogle(@RequestBody GoogleAuthRequest request) {
        try {
            String idToken = request.getCredential();

            // 使用 Google 官方库验证 ID Token
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), jsonFactory)
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid ID token"));
            }

            // 从 token 中提取 payload
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // 创建或更新用户（存储逻辑省略）
            Map<String, Object> user = new HashMap<>();
            user.put("id", payload.getSubject());
            user.put("email", email);
            user.put("fullName", name != null ? name : email.split("@")[0]);
            user.put("role", "USER");

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    public static class GoogleAuthRequest {
        private String credential;

        public String getCredential() {
            return credential;
        }

        public void setCredential(String credential) {
            this.credential = credential;
        }
    }
}
```

#### 2. CORS 配置

```java
package com.stinc2025.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

#### 3. pom.xml 依赖（添加 Google Auth 库）

```xml
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>1.33.0</version>
</dependency>
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client-gson</artifactId>
    <version>1.33.0</version>
</dependency>
```

---

## 常见问题 (FAQ)

**Q: 我的 Client ID 是多少？**
A: 你的 Client ID 是 `885940597719-mtcoo82k9bbksvuj786nsj7iombqtsli.apps.googleusercontent.com`，已在 `login.html` 中配置。

**Q: 我需要在后端验证 ID Token 吗？**
A: 是的，在生产环境中必须验证。Mock 后端目前只是解码（不验证签名）以便本地测试。对于真正的生产部署，使用 Google 官方库（如 Spring Boot 中的 `GoogleIdTokenVerifier`）来验证 token。

**Q: 测试时我应该用哪个谷歌账号？**
A: 任何有效的 Google 账号都可以。如果你想限制只有特定用户能登录，可以在后端逻辑中检查 email。

**Q: 我可以在不同的端口上测试吗？**
A: 可以，但你需要：
1. 在 Google Cloud Console 中添加那个新的 origin（例如 `http://localhost:3000`）
2. 在该端口上启动你的前端服务器

**Q: 我应该把后端代码提交到仓库吗？**
A: 这取决于你的项目结构。通常，后端代码应该在一个独立的仓库中，或者如果在同一个仓库中，应该在 `server/` 目录下（已做好准备）。

---

## 下一步

1. ✅ 在 Google Cloud Console 中添加 `http://127.0.0.1:5500` 作为 Authorized JavaScript origin
2. ✅ 启动本地后端（`node server.js`）
3. ✅ 在浏览器中测试 Google Sign-In 流程
4. 如果成功，用户应该被重定向到 `home.html`，并且他们的信息应该保存到 localStorage

祝你测试顺利！如有任何问题，检查浏览器 Console 中的错误信息。

