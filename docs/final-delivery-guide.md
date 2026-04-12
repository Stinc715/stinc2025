# Final Delivery Guide

## Project overview

Club Portal 是一个围绕俱乐部场景构建的综合型门户项目，覆盖用户注册登录、会员与俱乐部主页、预约、支付、聊天、Community Q&A 和 onboarding 等核心流程。

当前提交采用前后端分离的多页面架构：

- frontend: 基于 Vite 的静态多页面前端
- backend: 基于 Spring Boot 3.2 / Java 17 的业务 API 和认证能力
- tests: 前端静态回归、桌面视觉回归、后端回归
- scripts: 交付打包、自检和跨平台运行辅助脚本

## What this submission already completes

本次提交已经完成并验证了以下内容：

- 前后端核心业务功能闭环
- 干净交付流程，支持生成源码型 submission tree
- 在首跑允许联网拉取 Playwright Chromium 和 Maven Wrapper 所需 Maven 发行包前提下，fresh submission 环境的可复现构建与测试
- 桌面端一致性共享样式层
- Playwright 桌面视觉回归
- 自动化交付自检 `npm run verify:submission`
- 审计文档、冻结说明和低风险回归护栏

## Supported acceptance scope

当前提交的验收范围明确限定为：

- Desktop only
- Chrome / Edge latest stable
- Browser zoom 100%
- Windows 为主验收环境
- 固定 viewport：
  - 1366x768
  - 1440x900
  - 1920x1080

## How to validate this handoff

评审或交接时，优先使用以下两条命令：

- `npm run test:acceptance`
  - 用途：执行当前仓库内的全量验收链，覆盖前端静态回归、后端回归和构建
- `npm run verify:submission`
  - 用途：重新生成 submission tree，并在该 fresh submission 目录内执行 `npm ci`、`build`、前端回归、视觉回归、后端回归和 acceptance，自带交付自检报告

补充说明：这里的 “fresh submission 可复现” 不是 “完全离线首跑可复现”。

- 评审机器仍需预先安装 Java 17；Maven Wrapper 只负责固定并下载 Maven，不会替代本机 Java
- 首次完整验收需要联网，用于拉取 Playwright Chromium 和 Maven Wrapper 使用的 Maven 发行包
- 这会影响 `npm run test:frontend:visual`、`npm run test:backend`、`npm run test:acceptance` 和 `npm run verify:submission`
- 后续可复用本机缓存；缓存就绪后，可在离线环境重复执行上述命令
- 因此，交付报告中的 PASS 表示该机器在验证时要么已具备这些缓存，要么允许了首次联网下载

## Intentionally retained items

以下内容是经过审计后刻意冻结的已知项，不视为“未完成”：

- `auth-session.js` 在 Vite build 中仍会触发 classic script warning，但当前是已知 non-blocking warning
- 高风险复杂页的 `.btn` 体系继续保留页内实现，不做强行统一

当前不继续推进这些点的原因是：在现阶段，稳定性、零功能回退和零视觉回退的优先级高于进一步清理这些高风险区域。

## Document index

- repo 入口与运行说明: `README.md`
- desktop consistency checklist: `docs/desktop-consistency-checklist.md`
- auth-session loading audit: `docs/auth-session-loading-audit.md`
- button system audit: `docs/button-system-audit.md`
- release freeze summary: `docs/release-freeze-summary.md`
- API contract: `backend/API_CONTRACT.md`
- backend guide: `backend/README.md`
- deploy guide: `deploy/README.md`
- tests guide: `tests/README.md`
