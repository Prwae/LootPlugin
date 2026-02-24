# LootPlugin - Hybrid Server Loot Generator

[Russian version below / Русская версия ниже](#lootplugin---генератор-лута-для-гибридных-серверов-ru)

Bukkit plugin for hybrid (Arclight, Mohist) and vanilla Minecraft servers. Generates customizable loot in Vanilla blocks, Item Frames, and Forge mod containers using NBT injection.

## Commands
Permission: `lootpl.admin`

* `/lootpl generate` - Populates marked containers and frames.
* `/lootpl assigner <type>` - Container marking tool.
* `/lootpl assignerframe <type>` - Frame marking tool.
* `/lootpl reload` - Reloads JSON files from disk.
* `/lootpl help` - Shows help menu.

## Usage
1. Configure `containers.json` and `frames.json` in the plugin folder.
2. Get assigner tool via `/lootpl assigner <type>`.
3. Left-click block/frame to mark. Right-click to unmark.
4. Run `/lootpl generate` to populate locations.

## Configuration Guide
* `size`: Total slots in the container (e.g., 27). Required for distribution in modded blocks.
* `pools`: Array of loot categories.
* `rolls`: Number of items picked from a pool per generation.
* `entries`: List of possible items.
* `weight`: Relative probability. Higher weight equals higher drop chance.
* `nbt`: Raw NBT data. Use `'` or `\"`  for internal strings: `"{display:{Name:'{\"text\":\"Name\"}'}}"`.

### Example: containers.json
```json
{
  "medical_crate": {
    "size": 9,
    "pools": [
      {
        "rolls": 2,
        "entries": [
          {
            "id": "minecraft:apple",
            "weight": 80,
            "min": 1,
            "max": 3,
            "nbt": "{display:{Name:'{\"text\":\"Sour Apple\",\"color\":\"green\"}'}}"
          },
          {
            "id": "minecraft:golden_apple",
            "weight": 10,
            "min": 1,
            "max": 1,
            "nbt": "{display:{Name:'{\"text\":\"Adrenaline\",\"color\":\"gold\"}'}}"
          }
        ]
      }
    ]
  },
  "tacz_arsenal": {
    "size": 15,
    "pools": [
      {
        "rolls": 1,
        "entries": [
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 20,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:glock_17',GunCurrentAmmoCount:17}"
          },
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 10,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:m4a1',GunCurrentAmmoCount:30,GunFireMode:'AUTO'}"
          }
        ]
      },
      {
        "rolls": 3,
        "entries": [
          {
            "id": "minecraft:iron_ingot",
            "weight": 20,
            "min": 1,
            "max": 6
          }
        ]
      }
    ]
  }
}

```

### Example: frames.json

```json
{
  "weapon_display": {
    "pools": [
      {
        "rolls": 1,
        "entries": [
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 50,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:ai_awp',GunCurrentAmmoCount:0}"
          },
          {
            "id": "minecraft:diamond_sword",
            "weight": 50,
            "min": 1,
            "max": 1,
            "nbt": "{Unbreakable:1b,display:{Name:'{\"text\":\"Machete\",\"color\":\"red\"}'}}"
          }
        ]
      }
    ]
  }
}

```

---

# LootPlugin - Генератор лута для гибридных серверов (RU)

Плагин Bukkit для гибридных (Arclight, Mohist) или ванильных серверов. Генерирует лут в ванильных блоках, рамках и модовых контейнерах Forge через инъекцию NBT.

## Команды

Разрешение: `lootpl.admin`

* `/lootpl generate` - Заполняет размеченные контейнеры и рамки.
* `/lootpl assigner <type>` - Инструмент разметки контейнеров.
* `/lootpl assignerframe <type>` - Инструмент разметки рамок.
* `/lootpl reload` - Перезагружает JSON файлы с диска.
* `/lootpl help` - Меню помощи.

## Использование

1. Настройте `containers.json` и `frames.json` в папке плагина.
2. Получите инструмент разметки через `/lootpl assigner <type>`.
3. Левый клик по блоку/рамке для разметки. Правый клик для снятия разметки.
4. Введите `/lootpl generate` для генерации лута.

## Руководство по конфигурации

* `size`: Количество слотов в контейнере (например, 27). Нужно для разброса лута в модовых блоках.
* `pools`: Массив категорий лута.
* `rolls`: Количество предметов, выбираемых из пула за одну генерацию.
* `entries`: Список возможных предметов.
* `weight`: Относительная вероятность выпадения.
* `nbt`: Данные NBT. Используйте `'` или `\"` внутри строки: `"{display:{Name:'{\"text\":\"Имя\"}'}}"`.

### Пример: containers.json

```json
{
  "medical_crate": {
    "size": 9,
    "pools": [
      {
        "rolls": 2,
        "entries": [
          {
            "id": "minecraft:apple",
            "weight": 80,
            "min": 1,
            "max": 3,
            "nbt": "{display:{Name:'{\"text\":\"Sour Apple\",\"color\":\"green\"}'}}"
          },
          {
            "id": "minecraft:golden_apple",
            "weight": 10,
            "min": 1,
            "max": 1,
            "nbt": "{display:{Name:'{\"text\":\"Adrenaline\",\"color\":\"gold\"}'}}"
          }
        ]
      }
    ]
  },
  "tacz_arsenal": {
    "size": 15,
    "pools": [
      {
        "rolls": 1,
        "entries": [
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 20,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:glock_17',GunCurrentAmmoCount:17}"
          },
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 10,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:m4a1',GunCurrentAmmoCount:30,GunFireMode:'AUTO'}"
          }
        ]
      },
      {
        "rolls": 3,
        "entries": [
          {
            "id": "minecraft:iron_ingot",
            "weight": 20,
            "min": 1,
            "max": 6
          }
        ]
      }
    ]
  }
}


```

### Пример: frames.json

```json
{
  "weapon_display": {
    "pools": [
      {
        "rolls": 1,
        "entries": [
          {
            "id": "tacz:modern_kinetic_gun",
            "weight": 50,
            "min": 1,
            "max": 1,
            "nbt": "{GunId:'tacz:ai_awp',GunCurrentAmmoCount:0}"
          },
          {
            "id": "minecraft:diamond_sword",
            "weight": 50,
            "min": 1,
            "max": 1,
            "nbt": "{Unbreakable:1b,display:{Name:'{\"text\":\"Machete\",\"color\":\"red\"}'}}"
          }
        ]
      }
    ]
  }
}
