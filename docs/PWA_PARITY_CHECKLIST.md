# Kudo PWA → Android 原生 1:1 还原清单

本清单以 `original/kudo_v2.2.0` 为唯一功能基准，用于约束 Android 原生版的功能、交互、视觉与数据格式。

## 1. 核心数据结构

- [ ] 保持与 PWA 备份 JSON 兼容的顶层状态结构
  - [ ] `coins`
  - [ ] `life`
  - [ ] `maxCoins`
  - [ ] `tasks`
  - [ ] `store`
  - [ ] `logs`
  - [ ] `recentVals`
  - [ ] `multiplier`
  - [ ] `listMode`
- [ ] `tasks` 项字段与原版兼容
  - [ ] `id`
  - [ ] `title`
  - [ ] `val`
  - [ ] `type` (`0=task`, `1=habit`)
  - [ ] `count`
  - [ ] `last`
  - [ ] `list` (`focus` / `inbox`)
  - [ ] `order`
- [ ] `store` 项字段与原版兼容
  - [ ] `id`
  - [ ] `title`
  - [ ] `cost`
  - [ ] `type` (`0=one-time`, `1=infinite`)
- [ ] `logs` 项字段与原版兼容
  - [ ] `t`
  - [ ] `txt`
  - [ ] `v`
  - [ ] `type`
  - [ ] `taskId`
  - [ ] `isHabit`
  - [ ] `itemData`
- [ ] 启动时执行原版同等的数据清洗
  - [ ] 缺失 `list` 的任务自动修正
  - [ ] 非法 `list` 值自动纠正为 `focus`
  - [ ] 缺失 `order` 自动补齐

## 2. 游戏机制

- [ ] 等级公式与原版一致：`floor(sqrt(life / 100)) + 1`
- [ ] 最终倍率公式与原版一致：基础倍率 × 等级倍率
- [ ] 任务完成时更新 `life`
- [ ] 任务完成时更新 `maxCoins`
- [ ] 根据最近 5 次任务价值调整 `multiplier`
  - [ ] 高于平均值 `+0.01`
  - [ ] 低于平均值 `-0.01`
  - [ ] 上限 `1.20`
  - [ ] 下限 `1.00`
- [ ] 奖励结算使用最终倍率并向下取整

## 3. 顶部 Header

- [ ] 固定顶部布局
- [ ] 显示 `LVL`
- [ ] 显示 XP 进度条
- [ ] 显示金币符号 `$`
- [ ] 金币数字带过渡动画
- [ ] 显示最终倍率标签
- [ ] 右上角设置按钮
- [ ] 浅色 / 深色主题下颜色一致

## 4. Dashboard 输入区

- [ ] 标题输入框
- [ ] 数值输入框
- [ ] 添加按钮
- [ ] 模式按钮
- [ ] 根据当前页面 / 列表状态自动切换 placeholder
  - [ ] `Tasks + Focus` → `Add to Focus...` / `10`
  - [ ] `Tasks + Inbox` → `Add to Inbox...` / `0`
  - [ ] `Store` → `Add to Store...` / `0`
- [ ] 拦截非法数字输入
  - [ ] 禁止 `-`
  - [ ] 禁止 `+`
  - [ ] 禁止 `e`
  - [ ] 禁止 `.`
- [ ] 空值时按 placeholder 默认值落库
- [ ] 强制正整数处理

## 5. Tab 导航

- [ ] 底部 3 个 Tab
  - [ ] `Tasks`
  - [ ] `Store`
  - [ ] `Log`
- [ ] 切换 Tab 时更新激活态颜色
- [ ] `Log` 页隐藏 Dashboard
- [ ] `Tasks` 页显示 Focus/Inbox Switcher
- [ ] 底栏视觉与原版一致

## 6. Tasks 页面

- [ ] Habits 区域
  - [ ] 标题 `Habits`
  - [ ] 可折叠 / 展开
  - [ ] 箭头旋转动画
  - [ ] 两列自适应卡片布局
- [ ] Focus / Inbox 切换器
  - [ ] 双按钮
  - [ ] 滑块动画
  - [ ] 空白区横滑切换
- [ ] Task 列表
  - [ ] 仅显示当前 `listMode` 对应任务
  - [ ] 空态文案 `Empty focus` / `Empty inbox`
  - [ ] 金额 badge 颜色与原版一致
  - [ ] 文本截断行为一致

## 7. Task 手势交互

- [ ] 右滑完成任务
- [ ] 左滑在 Focus / Inbox 之间流转
- [ ] 左滑时展示蓝色背景
- [ ] 右滑时展示绿色背景
- [ ] 到达阈值后执行飞出动画
- [ ] 未达阈值时回弹
- [ ] 点击任务打开编辑弹层
- [ ] 长按任务进入拖拽排序
- [ ] 排序完成后持久化顺序
- [ ] Inbox → Focus 且价值为 `0` 时弹出“先定价再流转”

## 8. Habit 交互

- [ ] 点击开始 1.5 秒充能
- [ ] 充能期间有填充动画
- [ ] 充能期间有递进式振动
- [ ] 再次点击可取消充能
- [ ] 完成后只更新 Header 与当前 Habit 卡片
- [ ] 当日完成显示 `done`
- [ ] 完成次数显示 `xN`
- [ ] 长按 500ms 进入 jiggle 模式
- [ ] jiggle 模式显示删除角标
- [ ] jiggle 模式支持拖拽排序
- [ ] 点击外部退出 jiggle 模式

## 9. Store 页面

- [ ] 列表标题 `Rewards Store`
- [ ] 奖励卡片 / 行样式与任务卡保持同语义
- [ ] 支持 `One-time` / `Infinite`
- [ ] 余额不足时降低透明度
- [ ] 左滑 / 右滑都可购买
- [ ] 购买成功飞出并记账
- [ ] 余额不足时红色报错回弹 + 振动
- [ ] 点击商店项打开编辑弹层

## 10. Log 页面

- [ ] 顶部 14 天趋势图
- [ ] 收入曲线
- [ ] 支出曲线
- [ ] 面积渐变填充
- [ ] 按日期分组日志
- [ ] 每行显示标题、时间、金额、撤销按钮
- [ ] 正数为绿色
- [ ] 负数为橙色
- [ ] 空态文案 `No history`

## 11. Undo 逻辑

- [ ] 撤销收入时扣回金币
- [ ] 撤销任务时回滚 `life`
- [ ] 撤销任务时回滚 `maxCoins`
- [ ] 撤销一次性任务时恢复任务
- [ ] 撤销习惯时回退 `count`
- [ ] 撤销商店消费时恢复金币
- [ ] 撤销一次性奖励时恢复商店项
- [ ] 删除对应日志记录

## 12. 编辑弹层

- [ ] 底部弹出式 sheet
- [ ] 标题输入
- [ ] 数值输入
- [ ] 保存按钮
- [ ] 删除按钮
- [ ] 点击关闭按钮关闭
- [ ] 点击背景关闭
- [ ] 编辑 Task / Habit / Store 三种数据
- [ ] 支持 Inbox → Focus 定价专用模式

## 13. Settings / Help

- [ ] 底部弹出式 settings sheet
- [ ] 显示 `Lifetime High`
- [ ] 显示 `Income / Expense Ratio`
- [ ] 浅色 / 深色主题切换
- [ ] `Backup Data`
- [ ] `Restore Data`
- [ ] `Help` 子页面切换
- [ ] Help 文案与原版一致
- [ ] Help / Settings 双视图切换按钮状态一致

## 14. 备份与恢复

- [ ] 导出 JSON 内容与原版兼容
- [ ] 导入原版 JSON 后可直接运行
- [ ] 导入前确认覆盖
- [ ] 导入后完整刷新状态
- [ ] Android 原生分享 / 文件导出体验

## 15. 视觉还原

- [ ] 浅色主题与 `style.css` 颜色值一致
- [ ] 深色主题与 `style.css` 颜色值一致
- [ ] 卡片圆角一致
- [ ] 间距体系一致
- [ ] Header / Bottom Bar 毛玻璃效果近似
- [ ] badge、边框、阴影、透明度接近原版
- [ ] 空态、图标、字号、字重接近原版

## 16. 原生能力

- [ ] 使用 Android 原生震动反馈
- [ ] 充能振动节奏还原
- [ ] 列表 / 卡片手势原生实现
- [ ] 不使用 WebView 包壳

## 17. 当前已识别的 Android 偏差

- [ ] `HomeScreen` 仍使用 mock data
- [ ] `Store` / `Log` 仍为占位页
- [ ] 当前 `StoreItemEntity` 不符合原版 `type` 语义
- [ ] 当前 `LogEntity` 缺少 `itemData`
- [ ] 当前完成任务逻辑未使用最终倍率
- [ ] 当前 UI 未接通真实状态流
- [ ] 当前缺少原版备份 JSON 兼容层
- [ ] 当前未实现原版核心手势链路

