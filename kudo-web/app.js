// --- Constants matching C code ---
const MAX_LOGS = 200;
const DATA_KEY = 'kudo_data_v1';

// --- State ---
let state = {
    coins: 0,
    tasks: [], // { id, title, value, priority, type (0=once, 1=habit) }
    desires: [], // { id, title, cost }
    logs: [] // { t: timestamp, d: desc, v: value, c: type }
};

// --- Utils ---
const generateId = () => Date.now() + Math.floor(Math.random() * 1000);

const saveState = () => {
    localStorage.setItem(DATA_KEY, JSON.stringify(state));
    updateUI();
};

const loadState = () => {
    const saved = localStorage.getItem(DATA_KEY);
    if (saved) {
        state = JSON.parse(saved);
    }
    updateUI();
};

const addLog = (typeCode, desc, val) => {
    state.logs.unshift({ // Add to beginning (Newest first)
        t: Math.floor(Date.now() / 1000),
        d: desc,
        v: val,
        c: typeCode
    });
    if (state.logs.length > MAX_LOGS) state.logs.pop();
};

// --- Core Actions ---

// Add Task
function addTask() {
    const titleIn = document.getElementById('task-title');
    const valIn = document.getElementById('task-val');
    const typeIn = document.getElementById('task-type');

    const title = titleIn.value.trim();
    const val = parseInt(valIn.value);
    
    if (!title || isNaN(val)) return alert("Invalid Input");

    state.tasks.push({
        id: generateId(),
        title: title,
        value: val,
        priority: 4, // Default for now, can extend UI later
        type: parseInt(typeIn.value)
    });

    titleIn.value = '';
    valIn.value = '';
    saveState();
}

// Add Store Item
function addStoreItem() {
    const titleIn = document.getElementById('store-title');
    const costIn = document.getElementById('store-cost');

    const title = titleIn.value.trim();
    const cost = parseInt(costIn.value);

    if (!title || isNaN(cost)) return alert("Invalid Input");

    state.desires.push({
        id: generateId(),
        title: title,
        cost: cost
    });

    titleIn.value = '';
    costIn.value = '';
    saveState();
}

// Do Task
function doTask(id) {
    const idx = state.tasks.findIndex(t => t.id === id);
    if (idx === -1) return;
    
    const t = state.tasks[idx];
    state.coins += t.value;
    addLog('D', t.title, t.value);

    // Remove if One-off (Type 0)
    if (t.type === 0) {
        state.tasks.splice(idx, 1);
    }

    saveState();
}

// Buy Item
function buyItem(id) {
    const item = state.desires.find(d => d.id === id);
    if (!item) return;

    if (state.coins >= item.cost) {
        state.coins -= item.cost;
        addLog('B', item.title, -item.cost);
        saveState();
    } else {
        alert("Not enough coins!");
    }
}

// Delete Item
function deleteItem(type, id) {
    if (!confirm("Delete this item?")) return;
    if (type === 'task') {
        state.tasks = state.tasks.filter(t => t.id !== id);
    } else {
        state.desires = state.desires.filter(d => d.id !== id);
    }
    saveState();
}

// --- UI Rendering ---

function updateUI() {
    // Header
    document.getElementById('balance-amount').innerText = state.coins;

    // Tasks
    const taskList = document.getElementById('task-list');
    taskList.innerHTML = '';
    state.tasks.forEach(t => {
        const div = document.createElement('div');
        div.className = `item-card ${t.priority === 1 ? 'prio-p1' : 'prio-def'}`;
        div.innerHTML = `
            <div class="item-left">
                <div class="val-badge" style="color:var(--accent-grn)">$${t.value}</div>
                <div>
                    <div>${t.title}</div>
                    ${t.type === 1 ? '<span class="habit-badge">Habit</span>' : ''}
                </div>
            </div>
            <div class="item-right">
                <button class="action-btn btn-do" onclick="doTask(${t.id})">DO</button>
                <button class="action-btn btn-del" onclick="deleteItem('task', ${t.id})">&times;</button>
            </div>
        `;
        taskList.appendChild(div);
    });

    // Store
    const storeList = document.getElementById('store-list');
    storeList.innerHTML = '';
    state.desires.forEach(d => {
        const div = document.createElement('div');
        div.className = 'item-card';
        div.innerHTML = `
            <div class="item-left">
                <div class="val-badge" style="color:var(--accent-red)">$${d.cost}</div>
                <div>${d.title}</div>
            </div>
            <div class="item-right">
                <button class="action-btn btn-buy" onclick="buyItem(${d.id})">BUY</button>
                <button class="action-btn btn-del" onclick="deleteItem('store', ${d.id})">&times;</button>
            </div>
        `;
        storeList.appendChild(div);
    });

    // Logs
    const logList = document.getElementById('log-list');
    logList.innerHTML = '';
    state.logs.forEach(l => {
        const date = new Date(l.t * 1000);
        const dateStr = `${(date.getMonth()+1).toString().padStart(2,'0')}-${date.getDate().toString().padStart(2,'0')} ${date.getHours()}:${date.getMinutes().toString().padStart(2,'0')}`;
        
        const div = document.createElement('div');
        div.className = 'log-entry';
        div.innerHTML = `
            <div>
                <span class="log-date">[${dateStr}]</span> 
                <span style="color:${l.c === 'D' ? 'var(--accent-blu)' : 'var(--accent-mag)'}">${l.c === 'D' ? 'DO' : 'BUY'}</span>
                <span>${l.d}</span>
            </div>
            <div class="log-val ${l.v > 0 ? 'pos' : 'neg'}">${l.v > 0 ? '+' : ''}${l.v}</div>
        `;
        logList.appendChild(div);
    });
}

// Navigation
function switchTab(tabName) {
    document.querySelectorAll('.view').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(el => el.classList.remove('active'));
    
    document.getElementById(`view-${tabName}`).classList.add('active');
    // Find button logic could be improved but simple works
    const btns = document.querySelectorAll('.tab-btn');
    if(tabName === 'tasks') btns[0].classList.add('active');
    if(tabName === 'store') btns[1].classList.add('active');
    if(tabName === 'log') btns[2].classList.add('active');
}

// Init
window.addEventListener('DOMContentLoaded', loadState);
