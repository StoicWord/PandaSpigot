# Titanium 1.0 [![Build](https://img.shields.io/github/actions/workflow/status/hpfxd/PandaSpigot/build.yml?branch=master&label=Build)](https://github.com/hpfxd/PandaSpigot/actions/workflows/build.yml) [![Servers](https://img.shields.io/bstats/servers/15154?label=Servers)](https://bstats.org/plugin/bukkit/PandaSpigot/15154)
PandaSpigot-based server for Minecraft 1.8.8, maintained by Hyouka Dev.

Discord: [dsc.gg/hyoukadev](https://dsc.gg/hyoukadev)

## Highlights
- **Backported API enhancements from newer versions**
    - Native Adventure API
    - ServerTickStartEvent & ServerTickEndEvent
    - PlayerChunkLoadEvent & PlayerChunkUnloadEvent
    - PlayerHandshakeEvent
    - EntityMoveEvent

- **Greatly improved network performance**
    - **Updating to Netty 4.1** offers the ability to use newer Java versions with epoll on Linux.
    - **Improved flush handling** to massively improve entity tracker performance.
    - **Support for Unix domain sockets** to avoid the overhead of TCP when using a proxy on the same machine.

- **More configuration options**, such as:
    - Customizable knockback
    - World and player data saving

See a full list of patches [here](./patches/).

## Using
Build Titanium locally with the commands below; the server JAR is written to
`paperclip.jar`. The build also creates
`build/distributions/Titanium-1.0.zip`, containing the server JAR, launch
scripts, the SWM class modifier, and Spark as a separate plugin at
`plugins/spark.jar`.

Run the distribution with `./run.sh` (Linux/macOS) or `run.bat` (Windows).
These launchers enable Paperclip's agent before startup so that, when a
SlimeWorldManager plugin JAR is already in `plugins/`, the class modifier
embedded in `paperclip.jar` is activated before server classes load. Bukkit
discovers and loads the SWM plugin as usual. If SWM is absent, Titanium starts
without the modifier. The SWM plugin itself is not bundled; install a version
compatible with 1.8.8. Java 8 users must use a launcher script; Java 9+ can
also start `paperclip.jar` directly and its launcher agent will check for SWM.

The bundled SWM class modifier is from the upstream GPL-3.0 project; its
license and corresponding source snapshot are included under
`licenses/slime-world-manager/` in the distribution.

For support, join the [Titanium Discord](https://dsc.gg/hyoukadev).

## API 
See our API patches [here](./patches/api/).  
[![Javadocs](https://repo.hpfxd.com/api/badge/latest/releases/com/hpfxd/pandaspigot/pandaspigot-api?name=Javadocs)](https://repo.hpfxd.com/javadoc/releases/com/hpfxd/pandaspigot/pandaspigot-api/1.8.8-R0.1-SNAPSHOT/raw/index.html)
<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>hpfxd-repo</id>
        <url>https://repo.hpfxd.com/releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.hpfxd.pandaspigot</groupId>
        <artifactId>pandaspigot-api</artifactId>
        <version>1.8.8-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
</details>

<details>
<summary>Gradle (kts)</summary>

```kotlin
repositories {
    mavenCentral()
    maven(url = "https://repo.hpfxd.com/releases/")
}

dependencies {
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
}
```
</details>

## Requirements
To build Titanium, you will need:
- JDK 17 or higher
- Git
- Bash

🧩 Although JDK 17 is required for building, the compiled JAR remains fully compatible with Java 8.

## Knockback configuration

PandaSpigot keeps the existing `friction`, `horizontal`, `vertical`,
`verticalLimit`, `extraHorizontal`, and `extraVertical` settings in
`pandaspigot.yml`. On startup it generates
`titanium/combat/kbdisplacement.yml`, which supports a `VANILLA` mode and
per-world named `CUSTOM` profiles. An existing root-level `kbdisplacement.yml`
is copied into the new location and retained as a backup. Profile values are
multipliers over those existing settings. Named profiles live under
`profiles`; select one globally with `worlds.default.profile` or for an
individual world with `worlds.<world>.profile`. Existing inline world settings
remain supported as a backwards-compatible fallback.

For example, create a sprint-only arena profile and assign it to one world:

```yaml
worlds:
  default:
    profile: default
  arena:
    profile: combos
profiles:
  default:
    mode: CUSTOM
    requiresSprint: false
  combos:
    mode: CUSTOM
    requiresSprint: true
    pullDown: 0.04
    normalMelee:
      horizontalMultiplier: 0.9
      verticalMultiplier: 0.9
    sprint:
      horizontalMultiplier: 0.95
```

Damage profiles cover melee, sprint and critical hits, arrows, snowballs,
eggs, fishing hooks, ender pearls, explosions, and other sources. Profiles only
change knockback where the 1.8.8 damage path applies it; projectile launch
speed and gravity are not modified. `horizontalDragMultiplier` and
`verticalDragMultiplier` control post-impact velocity retention; `pullDown`
adds a small downward counter-impulse at impact before vanilla gravity takes
over. `requiresSprint: true` uses the vanilla path for a non-sprinting player
hit and applies the custom profile to sprint hits. Values are validated and
clamped to safe supported ranges on load.

### Titanium commands

The `titanium/` folder is generated on first start. `settings.yml` controls the
brand strings, `&d`/`&7` theme, and command toggles;
`combat/kbdisplacement.yml` holds named knockback profiles and per-world
assignments. Existing root-level `kbdisplacement.yml` files are copied into
that folder and retained.

- `/titanium` shows the branded command menu.
- `/titanium reload` reloads Titanium settings and combat profiles;
  `/titanium save` saves them. `/titanium theme <primary> [secondary]` changes
  the legacy color codes (for example `&d &7`). These management actions
  require `titanium.settings.manage` (operators by default). Command enable
  toggles still require a restart.
- `/kb` displays the active world's profile; `/kb getdefault`, `/kb list`, and
  `/kb view <profile>` browse profiles. Profile values are clickable and
  suggest an edit command.
- `/combat` displays the profile-management help. `/combat list`, `create`,
  `edit`, `remove`, `set`, `view`, `viewProjectile`, `world`, `setdefault`,
  `save`, and `reload` manage named profiles and per-world assignments.
  Settings such as `normalMelee.horizontal`, `sprint.vertical`,
  `requiresSprint`, `resetSprint`, and `pullDown` can be edited. The command
  requires `titanium.knockback.view` (operators by default).
- `/plugin enable|disable|load|reload <plugin>` manages plugin lifecycle and
  loads JARs from the `plugins/` directory. It requires the
  `titanium.plugin.manage` permission (operators by default). `reload` runs the
  plugin's disable/enable lifecycle; it does not replace its classloader or
  update already loaded code.
- `/tps` shows 10-second and 1/5/15-minute TPS, average tick time, and player
  count. Its clickable `[Server Info]` opens `/tps info`.
- The official [Spark Bukkit plugin v1.10.196](https://ci.lucko.me/job/spark/548/artifact/spark-bukkit/build/libs/spark-1.10.196-bukkit.jar)
  is embedded and installed into `plugins/spark.jar` on startup when no Spark
  JAR is present; `/spark` uses its native command. `/ver` reports Titanium as
  the server implementation, and the 1.8 client brand is `Titanium`.

### Panda Bash build script
The `panda` Bash script is the entry point for setup, patching, and builds.
Run it from a Bash-compatible shell on your platform. Run `./panda` without
arguments to print its command list.

Normal commands:

| Command | Description |
| --- | --- |
| `rb`, `rebuild` | Rebuild patches, can be called from anywhere. |
| `setup` | Remap, decompile, and patch Minecraft. Can be run from anywhere. |
| `p`, `patch` | Apply all patches to the project without building it. Can be run from anywhere. |
| `j`, `jar` | Build the project, `paperclip.jar` will be output. Can be run from anywhere. |
| `c`, `clean` | Removes build files under `PandaSpigot-API`, `PandaSpigot-Server`. |
| `dc`, `distclean` | Removes all generated files, `PandaSpigot-API`, `PandaSpigot-Server`, `base`, and `work`. |
| `con`, `continue` | Shortcut command for running `git am --continue` or `git rebase --continue`. |

Commands that require `. ./panda install` first:

| Command | Description |
| --- | --- |
| `r`, `root` | Change directory to the root of the project. |
| `a`, `api` | Move to the `PandaSpigot-API` directory. |
| `s`, `server` | Move to the `PandaSpigot-Server` directory. |
| `e`, `edit` | Use to edit a specific patch, give it the argument `server` or `api` respectively to edit the correct project. Use the argument `continue` after the changes have been made to finish and rebuild patches. Can be called from anywhere. |
| `install` | Add an alias to `$RCPATH` to allow full functionality of this script. |

Install the default `panda` alias with:

```bash
. ./panda install
```

You can also provide a custom alias name:

```bash
. ./panda install example
```

`PandaSpigot-API` and `PandaSpigot-Server` are generated work trees created by
the build scripts. The patch creation process follows Paper's contributing guide,
which is linked below; `./panda rb` is the PandaSpigot command that saves those
work tree commits back into `patches/api` and `patches/server`.

### Patch workflow
PandaSpigot uses a patch-based workflow. Changes are committed once inside the
generated work tree, then committed again from the repository root after the
patch files are rebuilt.

Use this order:

1. Make your changes in `PandaSpigot-API` or `PandaSpigot-Server`.
2. Commit those changes inside that generated work tree.
3. Return to the repository root and run `./panda rb`.
4. Review the regenerated files in `patches/api` or `patches/server`.
5. Commit the regenerated patch files from the repository root.

Only push commits from the repository root. Do not push or pull from
`PandaSpigot-API` or `PandaSpigot-Server`; their commits are only used to create
patch files. Commit work tree changes before rebuilding or reapplying patches,
because those commands can reset the generated work trees.

## Contributing
You can mostly follow [Paper's contributing guide](https://github.com/PaperMC/Paper-archive/blob/ver/1.16.5/CONTRIBUTING.md), just remember:
- Multi-line changes start with `// PandaSpigot start` and end with `// PandaSpigot end`
- If the change isn't obvious, add a small explanation like this: `// PandaSpigot start - reason`
- One-line changes should have `// PandaSpigot` at the end of the line.
- Follow Java code style (aka. Oracle style), with some exceptions:
  - If you are modifying upstream files, keep your diff size minimal. Going over 80 characters per line is fine to make this happen.
  - When in doubt or the code around your change is in a clearly different style, use the same style as the surrounding code.

When contributing, please think about the side effects of any changes you write.
Plugin compatibility is important, and we wish to minimize any breakage.

Please do not open pull requests for features that you cannot justify the existence of,
and the added maintenance costs of that come along with them. If you are thinking of
adding a feature that may be controversial, please open an issue first!
