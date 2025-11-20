# Kudo

> A Minimalist CLI Gamification Tracker for Self-Discipline and Rewards.

**Kudo** is a distraction-free, command-line utility designed to gamify your personal productivity. It operates on a simple economy: complete tasks and habits to earn 'Kudos' (coins), which can then be spent on personalized rewards.

Built in pure C, Kudo is fast, lightweight, and ideal for any terminal environment on Linux, macOS, and BSD systems.

## ✨ Features

* **Task & Habit Management**: Dedicated commands for single-instance tasks (`add`) and recurring habits (`habit`).
* **Minimalist Interface**: Clean, color-coded output for priorities (P1-P4) using standard ANSI escape codes.
* **Reward Economy**: Use earned Kudos to purchase desires defined in the integrated store.
* **Zero Dependencies**: Written entirely in C, ensuring maximum speed and portability.
* **JSON Persistence**: Data is stored locally in `~/.config/kudo/data.json`, easy to backup.
* **Tiling WM Ready**: A dedicated `status` command provides compact output for custom status bars (e.g., DWM, Polybar).

## 🛠️ Installation

Kudo requires only a standard C compiler (`gcc` or `clang`).

1.  **Compile the source:**
    ```bash
    gcc kudo.c -o kudo
    ```

2.  **Install the binary:**
    Move the executable to a directory in your system's PATH (e.g., `~/.local/bin/`).
    ```bash
    mv kudo ~/.local/bin/
    ```

## 📖 Usage & Commands

Run `kudo` or `kudo ls` to view your dashboard.

| Command | Alias | Syntax | Description |
| :--- | :--- | :--- | :--- |
| **`list`** | `ls` | `kudo ls` | Show all tasks, rewards, and current balance. |
| **`add`** | `a` | `kudo add "Title" <value> [prio 1-4]` | Add a one-off task (default P4). |
| **`habit`** | `h` | `kudo habit "Title" <value>` | Add a recurring habit (default P4). |
| **`do`** | | `kudo do <ID>` | Complete a task/habit and earn Kudos. |
| **`rm`** | `remove` | `kudo rm <ID>` | Permanently delete a task or store item. |
| **`store`** | | `kudo store "Item" <cost>` | Add a personalized reward to the store. |
| **`buy`** | `b` | `kudo buy <ID>` | Purchase a reward and spend Kudos. |
| **`status`** | `st` | `kudo status` | Get compact output for status bars. |

### Examples

```bash
# Add a high-priority task (P1)
kudo add "Finalize Report" 40 1

# Add a daily habit
kudo habit "Meditate 10min" 5

# Add a reward item
kudo store "Buy new mechanical keyboard" 250

# Complete task ID 3
kudo do 3
```

## 🖥️ Status Bar Integration

The **`kudo status`** command is designed to provide minimal, non-intrusive output for scripts driving status bars like **Polybar, DWM, or tmux**.

**Example output:** `3 tasks | $120`

You can pipe this directly into your status script:

```bash
#!/bin/bash
KUDO=$(kudo status)
DATE=$(date +'%H:%M %b %d')
echo "${KUDO} | ${DATE}"
```

## 📂 Data

Your data is stored locally in: `~/.config/kudo/data.json`

## ⚖️ License

This project is licensed under the MIT License.
