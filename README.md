# ⚡ TitanHammer Pro v1.0.0

**Advanced Custom Netherite Axe Plugin for Paper 1.21**

A fully-featured, professional-grade Minecraft plugin featuring a custom Netherite Axe with leveling system, skill trees, block filtering, multi-economy support, and MythicMobs integration.

---

## ✨ Features

### Core
- **Custom Netherite Axe** with `CustomModelData: 2025` for resource pack support
- **Player Level System** with XP storage per UUID (YAML-based persistence)
- **Shift + Right Click** opens the main GUI menu
- **OOP Architecture** with clean manager-based structure
- **Full Arabic & English** language support

### Skill Tree System (10 Skills, 3 Tiers)
| Tier | Skill | Description |
|------|-------|-------------|
| 1 | Damage Boost | +10% damage per level (max 10) |
| 1 | XP Boost | +15% XP per level (max 10) |
| 1 | Fortune | +20% extra drops per level (max 5) |
| 2 | Swift Miner | Mining speed boost (requires XP Boost) |
| 2 | Haste Aura | Grants Haste effect (requires Swift Miner) |
| 2 | Inferno Strike | Sets enemies on fire (requires Damage Boost) |
| 3 | Earthquake | Area break (requires Fortune + Swift Miner) |
| 3 | Auto Smelt | Smelts ores automatically (requires Fortune) |
| 3 | Vampiric Strike | Life steal (requires Damage Boost + Inferno) |
| 3 | Drop Master | Multiplies mob drops (requires Fortune) |

### Block Filter System
- **Auto Collect Mode** — Selected blocks go directly to inventory
- **Auto Delete Mode** — Selected blocks are removed without drops
- Paginated GUI with 40+ configurable block types
- Per-player filter configuration

### Economy Support
- **Vault** (any Vault-compatible economy)
- **PlayerPoints**
- **Custom** built-in economy

### Integrations
- **MythicMobs** — Damage bonus scaling + drop bonus with mob level
- Full compatibility with protection plugins via event system

### Anti-Exploit Protection
- Silk Touch application prevention
- Anvil/Grindstone/Smithing table duplication blocking
- Creative mode protection
- Owner-locked hammers (per UUID)
- Drag-duplication prevention

### Effects
- Custom particles for every action
- Sound effects (configurable)
- Level-up fireworks with totem particles
- Action-specific feedback (collect, delete, smelt, heal)

---

## 📁 Project Structure

```
TitanHammerPro/
├── pom.xml
└── src/main/
    ├── java/com/titanhammer/
    │   ├── TitanHammerPro.java          # Main plugin class
    │   ├── commands/
    │   │   └── TitanCommand.java        # Command handler + tab completion
    │   ├── economy/
    │   │   └── EconomyManager.java      # Vault/PlayerPoints/Custom
    │   ├── gui/
    │   │   ├── MainGUI.java             # Main menu
    │   │   ├── SkillTreeGUI.java        # Skill tree with tiers
    │   │   ├── UpgradeGUI.java          # Quick upgrade interface
    │   │   └── BlockFilterGUI.java      # Paginated block filter
    │   ├── hooks/
    │   │   └── MythicMobsHook.java      # MythicMobs integration
    │   ├── listeners/
    │   │   ├── PlayerInteractListener.java
    │   │   ├── BlockBreakListener.java
    │   │   ├── GUIClickListener.java
    │   │   ├── PlayerJoinQuitListener.java
    │   │   ├── AntiExploitListener.java
    │   │   ├── EntityDamageListener.java
    │   │   └── EntityDeathListener.java
    │   ├── managers/
    │   │   ├── ConfigManager.java
    │   │   ├── MessageManager.java
    │   │   ├── PlayerDataManager.java
    │   │   ├── ItemManager.java
    │   │   ├── SkillManager.java
    │   │   ├── BlockFilterManager.java
    │   │   └── EffectsManager.java
    │   ├── models/
    │   │   ├── PlayerProfile.java
    │   │   └── Skill.java
    │   └── utils/
    │       └── GUIUtils.java
    └── resources/
        ├── plugin.yml
        ├── config.yml
        ├── messages_en.yml
        └── messages_ar.yml
```

---

## 🔧 Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/titanhammer` | `titanhammer.use` | Open main GUI |
| `/titanhammer give [player]` | `titanhammer.admin` | Give TitanHammer |
| `/titanhammer setlevel <player> <level>` | `titanhammer.admin` | Set player level |
| `/titanhammer addxp <player> <amount>` | `titanhammer.admin` | Add XP to player |
| `/titanhammer info [player]` | `titanhammer.use` | View player info |
| `/titanhammer lang <en/ar>` | `titanhammer.use` | Change language |
| `/titanhammer reload` | `titanhammer.admin` | Reload configuration |

**Aliases:** `/th`, `/titan`

---

## 📦 Building

```bash
mvn clean package
```

The compiled JAR will be in `target/TitanHammer Pro-1.0.0.jar`

---

## ⚙️ Requirements

- **Paper 1.21+** (not Spigot — uses Paper API features)
- **Java 21+**
- **Optional:** Vault, PlayerPoints, MythicMobs

---

## 📝 Configuration

All settings are in `config.yml` with detailed comments. Key options:
- Custom model data value
- XP scaling formula
- Economy type selection
- Effect toggles
- Anti-exploit toggles
- Auto-save interval
- Default language

---

*Developed by Blooddev for PrimeHost*
