#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <time.h> // <-- 新增: 用于时间戳

/*
 * KUDO - Minimalist Gamification Tracker
 * Philosophy: Frictionless discipline.
 */

#define MAX_TASKS 100
#define MAX_DESIRES 50
#define MAX_STR 256
#define DATA_DIR ".config/kudo"
#define DATA_FILE "data.json"
#define LOG_MAX 200 // 定义最大日志条目数

// ANSI Colors
#define COL_RESET "\033[0m"
#define COL_RED   "\033[1;31m"
#define COL_YEL   "\033[1;33m"
#define COL_GRN   "\033[1;32m"
#define COL_GRY   "\033[90m"
#define COL_BLU   "\033[1;34m"
#define COL_MAG   "\033[1;35m" // 新增颜色用于日志

typedef enum { P1 = 1, P2, P3, P4 } Priority;
typedef enum { ONE_OFF = 0, HABIT = 1 } TaskType;

typedef struct {
    int id;
    char title[MAX_STR];
    int value;
    Priority priority;
    TaskType type;
    int completed; 
} Task;

typedef struct {
    int id;
    char title[MAX_STR];
    int cost;
} Desire;

// --- 新增: 日志结构体 ---
typedef struct {
    long timestamp; // 时间戳（Unix Time）
    char description[MAX_STR]; // 日志描述 (任务/奖励名称)
    int value; // 变动值 (+ coins for do, - coins for buy)
    char type_code; // 类型代码 ('D' for Done, 'B' for Buy)
} LogEntry;


typedef struct {
    int coins;
    Task tasks[MAX_TASKS];
    int task_count;
    Desire desires[MAX_DESIRES];
    int desire_count;
    // --- 新增: 日志状态 ---
    LogEntry logs[LOG_MAX];
    int log_count;
} AppState;

// --- UTILS ---

void get_file_path(char *buffer) {
    const char *home = getenv("HOME");
    if (!home) home = "/tmp";
    snprintf(buffer, MAX_STR * 2, "%s/%s/%s", home, DATA_DIR, DATA_FILE);
}

void ensure_directory() {
    char path[MAX_STR * 2];
    const char *home = getenv("HOME");
    if (!home) home = "/tmp";
    snprintf(path, MAX_STR * 2, "%s/%s", home, DATA_DIR);
    
    struct stat st = {0};
    if (stat(path, &st) == -1) {
        char cmd[MAX_STR * 3];
        snprintf(cmd, sizeof(cmd), "mkdir -p \"%s\"", path);
        system(cmd);
    }
}

int is_cmd(const char *arg, const char *cmd, const char *alias) {
    return (strcmp(arg, cmd) == 0 || (alias && strcmp(arg, alias) == 0));
}

// --- LOGGING ---

long get_current_timestamp() {
    return (long)time(NULL); 
}

void add_log_entry(AppState *state, char type_code, const char *description, int value) {
    if (state->log_count >= LOG_MAX) {
        // Log is full. Shift remove the oldest log (FIFO)
        for(int i=0; i<state->log_count-1; i++) state->logs[i] = state->logs[i+1];
        state->log_count = LOG_MAX - 1;
    }

    LogEntry *l = &state->logs[state->log_count];
    l->timestamp = get_current_timestamp();
    snprintf(l->description, MAX_STR, "%s", description);
    l->value = value;
    l->type_code = type_code;
    state->log_count++;
}


// --- STATE MANAGEMENT ---

void save_state(AppState *state) {
    char path[MAX_STR * 2];
    get_file_path(path);
    ensure_directory();

    FILE *f = fopen(path, "w");
    if (!f) { perror("Save failed"); return; }

    fprintf(f, "{\n  \"coins\": %d,\n", state->coins);
    
    // Write Tasks
    fprintf(f, "  \"tasks\": [\n");
    int active_count = 0;
    for(int i=0; i<state->task_count; i++) {
        if(!state->tasks[i].completed || state->tasks[i].type == HABIT) active_count++;
    }

    int printed = 0;
    for (int i = 0; i < state->task_count; i++) {
        // Skip completed one-offs (garbage collection)
        if (state->tasks[i].completed && state->tasks[i].type == ONE_OFF) continue;
        
        fprintf(f, "    { \"id\": %d, \"title\": \"%s\", \"value\": %d, \"priority\": %d, \"type\": %d }%s\n",
            state->tasks[i].id, state->tasks[i].title, state->tasks[i].value, 
            state->tasks[i].priority, state->tasks[i].type,
            (printed < active_count - 1) ? "," : ""
        );
        printed++;
    }
    fprintf(f, "  ],\n");

    // Write Desires
    fprintf(f, "  \"desires\": [\n");
    for (int i = 0; i < state->desire_count; i++) {
        fprintf(f, "    { \"id\": %d, \"title\": \"%s\", \"cost\": %d }%s\n",
            state->desires[i].id, state->desires[i].title, state->desires[i].cost,
            (i < state->desire_count - 1) ? "," : ""
        );
    }
    fprintf(f, "  ],\n"); // <-- 注意：这里需要逗号，因为后面还有 logs

    // --- 新增: Write Logs ---
    fprintf(f, "  \"logs\": [\n");
    for (int i = 0; i < state->log_count; i++) {
        // 使用缩写字段名 t:timestamp, d:description, v:value, c:type_code
        fprintf(f, "    { \"t\": %ld, \"d\": \"%s\", \"v\": %d, \"c\": \"%c\" }%s\n",
            state->logs[i].timestamp, state->logs[i].description, state->logs[i].value, state->logs[i].type_code,
            (i < state->log_count - 1) ? "," : ""
        );
    }
    fprintf(f, "  ]\n}\n");
    // -------------------------

    fclose(f);
}

void load_state(AppState *state) {
    char path[MAX_STR * 2];
    get_file_path(path);
    
    state->coins = 0;
    state->task_count = 0;
    state->desire_count = 0;
    state->log_count = 0; // <-- 初始化

    FILE *f = fopen(path, "r");
    if (!f) return;

    char line[2048];
    int in_tasks = 0, in_desires = 0, in_logs = 0; // <-- 新增: in_logs

    while (fgets(line, sizeof(line), f)) {
        if (strstr(line, "\"coins\":")) {
            sscanf(line, "  \"coins\": %d,", &state->coins);
        } else if (strstr(line, "\"tasks\": [")) {
            in_tasks = 1; in_desires = 0; in_logs = 0;
        } else if (strstr(line, "\"desires\": [")) {
            in_tasks = 0; in_desires = 1; in_logs = 0;
        } else if (strstr(line, "\"logs\": [")) { // <-- 新增: Logs 块识别
            in_tasks = 0; in_desires = 0; in_logs = 1;
        } else if (strchr(line, ']')) {
            in_tasks = 0; in_desires = 0; in_logs = 0;
        } else if (strchr(line, '{')) {
            if (in_tasks && state->task_count < MAX_TASKS) {
                Task *t = &state->tasks[state->task_count];
                t->completed = 0;
                char *start = strchr(line, '{');
                sscanf(start, "{ \"id\": %d, \"title\": \"%[^\"]\", \"value\": %d, \"priority\": %d, \"type\": %d }",
                       &t->id, t->title, &t->value, (int*)&t->priority, (int*)&t->type);
                state->task_count++;
            } else if (in_desires && state->desire_count < MAX_DESIRES) {
                Desire *d = &state->desires[state->desire_count];
                char *start = strchr(line, '{');
                sscanf(start, "{ \"id\": %d, \"title\": \"%[^\"]\", \"cost\": %d }",
                       &d->id, d->title, &d->cost);
                state->desire_count++;
            } else if (in_logs && state->log_count < LOG_MAX) { // <-- 新增: Log 加载逻辑
                LogEntry *l = &state->logs[state->log_count];
                char *start = strchr(line, '{');
                sscanf(start, "{ \"t\": %ld, \"d\": \"%[^\"]\", \"v\": %d, \"c\": \"%c\" }",
                        &l->timestamp, l->description, &l->value, &l->type_code);
                state->log_count++;
            }
        }
    }
    fclose(f);
}

int get_next_id(AppState *state) {
    int max = 0;
    for(int i=0; i<state->task_count; i++) if(state->tasks[i].id > max) max = state->tasks[i].id;
    for(int i=0; i<state->desire_count; i++) if(state->desires[i].id > max) max = state->desires[i].id;
    return max + 1;
}

// --- CORE LOGIC ---

void print_header(const char* text) {
    printf(COL_GRY "\n--- %s ---\n" COL_RESET, text);
}

void cmd_list(AppState *state) {
    print_header("TODO");
    if (state->task_count == 0) printf(COL_GRY "  (No active tasks)\n" COL_RESET);

    for(int i=0; i<state->task_count; i++) {
        Task t = state->tasks[i];
        if (t.completed && t.type == ONE_OFF) continue;

        char flags[10] = "   ";
        if (t.type == HABIT) strcpy(flags, "(R)");

        const char *col = COL_RESET;
        if (t.priority == P1) col = COL_RED;
        else if (t.priority == P2) col = COL_YEL;

        printf("%s%3d %s | $%d \t %s%s\n", col, t.id, flags, t.value, t.title, COL_RESET);
    }

    print_header("STORE");
    for(int i=0; i<state->desire_count; i++) {
        Desire d = state->desires[i];
        printf(COL_BLU "%3d" COL_RESET "     | $%d  %s\n", d.id, d.cost, d.title);
    }
    
    printf("\n" COL_GRN "BALANCE: $%d" COL_RESET "\n", state->coins);
}

void cmd_add(AppState *state, char *title, int val, int prio, TaskType type) {
    if (state->task_count >= MAX_TASKS) { printf("Task limit reached.\n"); return; }
    Task *t = &state->tasks[state->task_count++];
    t->id = get_next_id(state);
    snprintf(t->title, MAX_STR, "%s", title);
    t->value = val;
    t->priority = (prio < 1 || prio > 4) ? 4 : prio; // Default P4
    t->type = type;
    t->completed = 0;
    printf("Created: [%d] %s\n", t->id, t->title);
    save_state(state);
}

void cmd_store(AppState *state, char *title, int cost) {
    if (state->desire_count >= MAX_DESIRES) return;
    Desire *d = &state->desires[state->desire_count++];
    d->id = get_next_id(state);
    snprintf(d->title, MAX_STR, "%s", title);
    d->cost = cost;
    printf("Store item added: [%d] %s\n", d->id, d->title);
    save_state(state);
}

void cmd_do(AppState *state, int id) {
    for(int i=0; i<state->task_count; i++) {
        if (state->tasks[i].id == id) {
            int value = state->tasks[i].value;
            char title[MAX_STR];
            snprintf(title, MAX_STR, "%s", state->tasks[i].title); // 复制标题
            
            state->coins += value;
            printf(COL_GRN "Done! " COL_RESET "'%s' (+$%d)\n", title, value);
            
            // --- 记录日志: 完成任务 ('D' for Done) ---
            add_log_entry(state, 'D', title, value);
            // ----------------------------------------

            if (state->tasks[i].type == ONE_OFF) {
                // Shift remove
                for(int j=i; j<state->task_count-1; j++) state->tasks[j] = state->tasks[j+1];
                state->task_count--;
            }
            save_state(state);
            return;
        }
    }
    printf("Task not found.\n");
}

void cmd_buy(AppState *state, int id) {
    for(int i=0; i<state->desire_count; i++) {
        if (state->desires[i].id == id) {
            int cost = state->desires[i].cost;
            char title[MAX_STR];
            snprintf(title, MAX_STR, "%s", state->desires[i].title); // 复制标题
            
            if (state->coins >= cost) {
                state->coins -= cost;
                printf(COL_YEL "Purchased: " COL_RESET "%s (-$%d)\n", title, cost);
                
                // --- 记录日志: 购买奖励 ('B' for Buy) ---
                add_log_entry(state, 'B', title, -cost); // 消费是负值
                // ----------------------------------------
                
                save_state(state);
            } else {
                printf(COL_RED "Too expensive." COL_RESET " Need $%d.\n", state->desires[i].cost);
            }
            return;
        }
    }
    printf("Item not found.\n");
}

void cmd_rm(AppState *state, int id) {
    // ... (cmd_rm 逻辑不变，删除操作不记录到活动日志)
    int found = 0;
    // Check tasks
    for(int i=0; i<state->task_count; i++) {
        if (state->tasks[i].id == id) {
            printf("Deleted task: %s\n", state->tasks[i].title);
            for(int j=i; j<state->task_count-1; j++) state->tasks[j] = state->tasks[j+1];
            state->task_count--;
            found = 1;
            break;
        }
    }
    // Check desires if not found in tasks
    if (!found) {
        for(int i=0; i<state->desire_count; i++) {
            if (state->desires[i].id == id) {
                printf("Deleted store item: %s\n", state->desires[i].title);
                for(int j=i; j<state->desire_count-1; j++) state->desires[j] = state->desires[j+1];
                state->desire_count--;
                found = 1;
                break;
            }
        }
    }
    
    if (found) save_state(state);
    else printf("ID %d not found.\n", id);
}

void cmd_log(AppState *state) {
    print_header("ACTIVITY LOG");
    if (state->log_count == 0) {
        printf(COL_GRY "  (No activity logged)\n" COL_RESET);
        return;
    }
    
    char date_str[20];
    
    // 从最新记录开始倒序打印
    for (int i = state->log_count - 1; i >= 0; i--) {
        LogEntry l = state->logs[i];
        
        // 转换时间戳为可读格式
        time_t timestamp = (time_t)l.timestamp;
        // 使用本地时间格式化
        strftime(date_str, sizeof(date_str), "%m-%d %H:%M", localtime(&timestamp));
        
        const char *col_value = (l.value > 0) ? COL_GRN : COL_RED;
        const char *action_label = (l.type_code == 'D') ? "DO" : "BUY";
        const char *col_action = (l.type_code == 'D') ? COL_BLU : COL_MAG;

        printf("%s%s %s[%s]%s %s%-4d %s%s%s\n", 
            COL_GRY, date_str, // 日期
            col_action, action_label, COL_RESET, // 动作 (DO/BUY)
            col_value, l.value, // 金币变动 (+-值)
            COL_GRY, l.description, COL_RESET // 描述
        );
    }
}


void cmd_status(AppState *state) {
    int active = 0;
    for(int i=0; i<state->task_count; i++) if(!state->tasks[i].completed) active++;
    printf("%d tasks | $%d", active, state->coins);
}

void help() {
    printf(COL_BLU "KUDO" COL_RESET " - Minimalist Tracker\n\n");
    printf("  ls, list           Show dashboard\n");
    printf("  st, status         One-line status (for bars)\n");
    printf("  log, l             Show activity log\n"); // <-- 新增
    printf("  do <id>            Complete task (earn coins)\n");
    printf("  buy <id>           Purchase reward (spend coins)\n");
    printf("  rm <id>            Delete task/item\n");
    printf("\n");
    printf("  add \"Title\" <val> [prio]  Add one-off task\n");
    printf("  habit \"Title\" <val>       Add recurring habit\n");
    printf("  store \"Item\" <cost>       Add reward to store\n");
    printf("\n");
    printf(COL_GRY "  Examples:\n");
    printf("    kudo add \"Fix Bug\" 20 1\n");
    printf("    kudo habit \"Workout\" 10\n" COL_RESET);
}

// --- MAIN ---

int main(int argc, char *argv[]) {
    AppState state;
    load_state(&state);

    if (argc < 2) {
        cmd_list(&state);
        return 0;
    }

    char *action = argv[1];

    if (is_cmd(action, "list", "ls")) {
        cmd_list(&state);
    }
    else if (is_cmd(action, "status", "st")) {
        cmd_status(&state);
    }
    else if (is_cmd(action, "log", "l")) { // <-- 新增: log 命令处理
        cmd_log(&state);
    }
    else if (is_cmd(action, "add", "a")) {
        if (argc < 4) { printf("Usage: add \"Title\" <value> [priority 1-4]\n"); return 1; }
        int prio = (argc >= 5) ? atoi(argv[4]) : 4;
        cmd_add(&state, argv[2], atoi(argv[3]), prio, ONE_OFF);
    }
    else if (is_cmd(action, "habit", "h")) {
        if (argc < 4) { printf("Usage: habit \"Title\" <value>\n"); return 1; }
        cmd_add(&state, argv[2], atoi(argv[3]), 4, HABIT); // Habits default to P4
    }
    else if (is_cmd(action, "store", NULL)) {
        if (argc < 4) { printf("Usage: store \"Item\" <cost>\n"); return 1; }
        cmd_store(&state, argv[2], atoi(argv[3]));
    }
    else if (is_cmd(action, "do", NULL)) {
        if (argc < 3) { printf("Usage: do <id>\n"); return 1; }
        cmd_do(&state, atoi(argv[2]));
    }
    else if (is_cmd(action, "buy", "b")) {
        if (argc < 3) { printf("Usage: buy <id>\n"); return 1; }
        cmd_buy(&state, atoi(argv[2]));
    }
    else if (is_cmd(action, "remove", "rm")) {
        if (argc < 3) { printf("Usage: rm <id>\n"); return 1; }
        cmd_rm(&state, atoi(argv[2]));
    }
    else {
        help();
    }

    return 0;
}
