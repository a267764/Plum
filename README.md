# PlumReloaded

> MiraiForum 经典插件 [Plum 梅花娘](https://mirai.mamoe.net/topic/241) 重制版

[![](https://shields.io/github/downloads/MrXiaoM/Plum/total)](https://github.com/MrXiaoM/Plum/releases) [![](https://img.shields.io/badge/mirai--console-2.12.3-blue)](https://github.com/mamoe/mirai)

# 介绍
Plum 是一款基于 mirai-console，使用 Java 语言编写的插件。由 MrXiaoM 使用 Kotlin 移植，在原来的基础上增加了 mirai 2.11+ 兼容，权限支持等等。

感谢 [SakuraWald 开源的 Plum](https://gitee.com/K85/plum)，原仓库使用 [The Unlicense](https://gitee.com/K85/plum/blob/master/LICENSE) 许可证，本仓库将改用 [GNU Affero General Public License v3.0](https://github.com/MrXiaoM/Plum/blob/main/LICENSE) 许可证。

# 功能
本插件移植了 Plum 的所有功能。

插件配置文件在 `./config/com.sakurawald.plum.reloaded/config.yml`，以下功能介绍中将会放出相应配置的位置 (因为配置嵌套加不了注释)，请按自己的需要进行修改

---
## 自动通过验证
```yaml
admin: 
  invitationManager: 
    friendInvitation:
      # 自动通过加好友验证
      autoAcceptAddQQFriend: false
    groupInvitation: 
      # 自动通过群申请
      autoAcceptAddQQGroup: false
```
## 每日5点
随机句子 + 倒计时
```yaml
functions:
  #...
  dailyCountdown: 
    # 是否启用该功能
    enable: true
    # 格式: 
    # 没到时提示还有$diff_days天|倒计时目标时间戳(秒)|到当天时显示的提示|结束了显示的提示
    # 你可以到 https://tool.chinaz.com/tools/unixtime.aspx 将时间转成时间戳
    countdownCommands: 
      - '◆距离2023年高考还有\$diff_days天！|1686067200|高考加油！&高考加油！&2023年高考已结束~'
      - '◆距离2023年考研还有\$diff_days天！|1671811200|考研加油&考研加油&考研加油&2023年考研已结束~'
      - '◆距离2023年四六级考试还有\$diff_days天！|1686758400|四六级考试加油！&四六级考试已结束~'
```
## 每日21点/诗词解读
每日诗词
```yaml
functions:
  #...
  dailyPoetry: 
    # 今日诗词 访问令牌
    # 虽然访问令牌是永久有效的，但推荐自行去以下链接免费申请一个
    # https://www.jinrishici.com/doc/#get-token
    jinRiShiCi: 
      token: paOa0DqOdpLn4FVVHNtEDgU5Imk89kXZ
    # 是否启用诗词解读功能
    explanationEnable: true
    maxRetryLimit: 3
```
诗词解读用法：`#解读 诗句`

## 戳一戳
随机一言

// TODO

## @+问题
AI聊天 (默认使用[青云客API](http://api.qingyunke.com/))

// TODO

## 点歌
支持**网易云音乐、酷狗音乐、QQ音乐**搜索，在搜索歌曲时将会按网易云音乐 → 酷狗音乐 → QQ音乐的顺序搜索。

可设置通过卡片还是语音来响应点歌命令。

若通过语音来响应，需要额外安装配置 [mirai-silk-converter](https://github.com/project-mirai/mirai-silk-converter) 来进行格式转换。

如果你需要QQ音乐平台的点歌功能，则要在本地服务器启用 QQMusicApi Node 服务，并确保该服务监听的端口是 3300。该服务可在
https://github.com/jsososo/QQMusicApi 下载。  
您也可以直接忽略该服务，则只使用 2 个音乐平台。

---

# 命令

| 命令 | 权限  | 描述     |
| --- |-----|--------|
| /plum reload | com.sakurawald.plum.reloaded:command.plumreloaded    | 重载配置文件 |

# 安装

到 [Releases](https://github.com/MrXiaoM/Plum/releases) 下载插件并放入 plugins 文件夹进行安装

> 2.11 或以上下载 PlumReloaded-*.mirai2.jar
>
> 2.11 以下下载 PlumReloaded-legacy-*.mirai.jar
安装完毕后，编辑配置文件作出你想要的修改。在控制台执行 `/plum reload` 重载配置即可~

# 移植

本次移植仅保证能跑，尽请期待后续优(挖)化(💩)和对 mirai 新版本生态的适配。

[![](https://img.shields.io/badge/PRs-welcome-00cc11)](https://github.com/MrXiaoM/Plum/pulls)
