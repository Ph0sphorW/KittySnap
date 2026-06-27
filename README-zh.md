# KittySnap
[[English](./README.md)] / [中文]  
_在 Napcat 和 Minecraft 之间转发消息！_  
KittySnap 是一个用于转发 QQ 群消息和游戏内消息的插件。

## 如何使用？
1. 从 actions 或者 release 中下载已编译的文件
   - 如果两者均不存在，你需要自行构建文件
2. 将插件放入你的 /plugin 文件夹里
3. 启动一个 [NapcatQQ](https://github.com/NapNeko/NapCatQQ) 服务器然后添加 `Websocket 服务端` 配置
   - 详细教程参阅 [NapcatQQ 官网](https://napneko.github.io/)
4. 打开 /plugin/KittySnap 然后打开 `config.yml`
5. 按照实际情况填写 `WsUrl`, `token`, 和 `groups`
6. 在游戏内运行 `/ks reload`
- 确保你的机器人在你的监听群里


## 构建
你需要 JDK 21 和良好的网络连接。  
- 打开终端运行如下命令行:
```text
git clone https://github.com/Ph0sphorW/KittySnap/
```
- 切换到下载的 git 存储库然后在该文件夹下打开终端
- 运行命令:
```text
.\gradlew build
```
- 等待
- 在 /target 文件夹下即可看到已构建好的文件

## PR
~~这是啥？~~

## 许可
查阅[许可证](./LICENSE)

## 待办事项
[] 消息积压机制  
[] standalone 和 client 模式，以及 typescript 版的中转服务端 (!?包装纸?!)  
[] 更完善的 plugin.yml  
[] 配置文件，语言文件部分修改  
[] placeholder api 支持  
[] 更多的合作项目支持  
别的么，等我想到了继续写。
## 鸣谢
[@XIAYM-gh](https://github.com/XIAYM-gh): 重新组织了文件结构  
[@HaHaWTH](https://github.com/HaHaWTH): 重构了图片处理部分
