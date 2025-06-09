# Clicktor -我的世界连点器MOD

一个用于 Minecraft 1.20.4 的自动连点器模组，基于 Fabric 框架开发。

## 功能特性

- **自动连点**：支持左键和右键自动连点
- **可配置间隔**：自定义点击间隔时间
- **持续时间控制**：设置每次点击的持续时间
- **点击次数限制**：可设置最大点击次数
- **随机间隔**：可启用随机间隔
- **快捷键控制**：支持自定义切换快捷键
- **GUI配置界面**：直观的配置界面

## 安装要求

- Minecraft 1.20.4
- Fabric Loader
- Fabric API 0.97.2+1.20.4

## 安装方法

1. 确保已安装 Fabric Loader 和 Fabric API
2. 将编译好的 mod jar 文件放入 `.minecraft/mods` 文件夹
3. 启动游戏

## 使用方法

1. 进入游戏后，按下快捷键（默认为H）访问配置选项
2. 根据需要调整点击间隔、持续时间等参数
3. 按下配置的快捷键（默认为F1）来开启/关闭自动连点器

## 构建方法

```bash
# 克隆项目
git clone <repository-url>
cd mouse-connector

# 构建项目
./gradlew build

# 生成的 jar 文件位于 build/libs/ 目录下
```

## 调试运行

```bash
# 运行开发环境客户端
./gradlew runClient

# 运行开发环境服务端
./gradlew runServer
```

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 注意事项

- 请遵守服务器规则，某些服务器可能禁止使用自动连点器
- 在多人游戏中，mod 会强制启用最小间隔限制（1000ms）以防止破坏公平


## 贡献

欢迎提交 Issue 和 Pull Request 来改进这个项目！

## 支持

如果您遇到任何问题，请在 GitHub Issues 中报告。
