# Kudo JSON Database

Kudo backups are ordinary UTF-8 JSON files. The file is designed to be both the app database and something a user can read or edit.

The public format is:

```json
{
  "format": "kudo",
  "version": 1,
  "settings": {
    "coins": 2309,
    "multiplier": 1,
    "taskSort": "manual"
  },
  "tasks": [],
  "store": [],
  "logs": [],
  "notes": []
}
```

Times are local strings with an offset, for example `2026-05-14T20:00:00+08:00`.
When writing by hand, `2026-05-14 20:00`, `2026-05-14T20:00`, and `2026-05-14` are also accepted as local time.

## Minimal Manual Log

Add a historical task log anywhere inside `logs`; Kudo sorts logs by time when importing.

```json
{
  "time": "2026-05-14 20:00",
  "kind": "task",
  "title": "Read paper notes",
  "coins": 25
}
```

Use a negative `coins` value and `"kind": "store"` for spending.

## Main Fields

- `settings.coins`: current coin balance
- `settings.multiplier`: current reward multiplier
- `settings.taskSort`: `autoDue` or `manual`
- `tasks`: active tasks and habits, in display order
- `store`: reward items
- `logs`: completed work and spending history
- `notes`: notebook entries

Tasks use `id`, `title`, `kind`, `coins`, optional `count`, optional `lastDone`, optional `due`, and optional `subtasks`.

Store items use `id`, `title`, `coins`, and `kind` (`once` or `repeatable`).

Logs use `time`, `kind`, `title`, `coins`, optional `baseCoins`, optional `subtaskId`, and optional `undo`.

`undo` is internal recovery data used by the app's undo action. Users do not need to write it for manual log entries.
