# Java游戏引擎(开发中)

这是一个基于Java和LWJGL开发的2D游戏引擎项目。该引擎提供了基础的2D渲染、物理模拟和游戏开发功能。

## 特性

- 基于LWJGL 3.3.2的现代OpenGL渲染
- 2D场景管理和渲染
- 使用JOML进行数学运算
- 完整的日志系统
- 跨平台支持（目前主要支持Windows）

## 技术栈

- Java 23
- LWJGL 3.3.2
- JOML 1.10.5
- Maven
- JUnit 4.13.2
- Logback 1.4.14

## 系统要求

- JDK 23或更高版本
- Maven 3.6.0或更高版本
- Windows操作系统（目前仅支持Windows平台）

## 安装说明

1. 克隆仓库：
```bash
git clone https://github.com/Fredrick-LX/JavaGameEngine.git
cd JavaGameEngine
```

2. 使用Maven构建项目：
```bash
mvn clean install
```

## 项目结构

```
JavaGameEngine/
├── core/                # 核心引擎模块
│   ├── src/             # 源代码
│   └── pom.xml          # Maven配置文件
├── .vscode/             # VS Code配置
└── README.md            # 项目文档
```

## 开发环境设置

1. 确保已安装JDK 23
2. 安装Maven
3. 配置IDE（推荐使用IntelliJ IDEA或VS Code）
4. 导入项目为Maven项目

## 使用示例

暂无

## 贡献指南

欢迎提交Pull Request或创建Issue来帮助改进项目。
