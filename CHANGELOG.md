## ⚙️ Changes

- Brigadier Commands for `reset` & `reset_all` have been modified.
  - They will reset the stats of the player regardless of the configuration.
  - Thus, a new argument has been created called `retain`. It is applied to these commands, and is fully optional. It allows you to choose how much of a percentage of stats you want to keep for the provided player(s).
  - Example: `/playerex reset <player_name> 50` will retain **50%** of the player's stats.