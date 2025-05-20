# CloudCore

A Minecraft proxy plugin for BungeeCord and Velocity that implements competitive cooldown restrictions. This plugin prevents players from joining competitive game servers for configurable time periods.

## Features

- **Universal Compatibility**: Works with both BungeeCord and Velocity proxies (single universal JAR)
- **Competitive Server Restrictions**: Prevent players from joining competitive servers during cooldown
- **Pattern-Based Server Matching**: Use wildcards to restrict server access (e.g., "bedwars*", "ranked*")
- **Modern UI**: Clean and professional message formatting with consistent styling
- **Flexible Storage**: Choose between MySQL database (with connection pooling) or file-based storage
- **Permission System**: Granular permission control with bypass capabilities

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/cc <player> <time> [reason]` | `cloudcore.admin` | Apply a competitive cooldown |
| `/cc check <player>` | `cloudcore.check` | Check cooldown status |
| `/cc list` | `cloudcore.admin` | List all active cooldowns |
| `/cc reset <player>` | `cloudcore.reset` | Remove a cooldown |
| `/cc help` | `cloudcore.admin` | Show command help |

**Aliases**: `/competitivecooldown`, `/compcool`, `/ccool`

## Time Format Examples

- `30s` - 30 seconds
- `5m` - 5 minutes
- `2h` - 2 hours
- `1d` - 1 day
- `7d` - 7 days
- `30d` - 30 days

## Configuration

The plugin uses a JSON configuration file. Here are the key sections:

### Server Restrictions

Here are how you can blacklist and whitelist servers!

Kind reminder: you can also use regex to block servers under more ids like bw-4, bw-3....... using bw* or other regex paterns!!
```json
"servers": {
  "restricted_servers": [
    "bedwars*",
    "skywars*",
    "competitive*",
    "ranked*",
    "tournament*"
  ],
  "exempt_servers": [
    "lobby",
    "hub",
    "practice"
  ]
}
```

### Storage Configuration
```json
"storage": {
  "type": "FILE",  // or "DATABASE"
  "database": {
    "host": "localhost",
    "port": 3306,
    "database": "cloudcore",
    "username": "root",
    "password": "password",
    "table_prefix": "mcc_"
  },
  "file": {
    "path": "cooldowns.json"
  }
}
```

### Message Customization
All messages support color codes and are fully customizable. Example format:
```
[CC]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
COMPETITIVE COOLDOWN
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

You have been restricted from competitive servers!
Duration: 30m
Reason: Administrative action

• Please follow our server rules
• Practice in non-competitive modes

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## Permissions

| Permission | Description |
|------------|-------------|
| `cloudcore.admin` | Full plugin access |
| `cloudcore.check` | Check cooldown status |
| `cloudcore.reset` | Reset cooldowns |
| `cloudcore.bypass` | Bypass restrictions |
| `cloudcore.notify` | Receive notifications |

## Support

For issues or questions:
1. Check the configuration guide above
2. Ensure proper permissions are set
3. Join our discord server: https://discord.mythical.systems
