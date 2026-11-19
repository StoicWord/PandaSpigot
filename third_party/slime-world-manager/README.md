# Slime World Manager class modifier

Titanium includes the official SWM class modifier for Minecraft 1.8.8, built
from the `slimeworldmanager-classmodifier` module at upstream commit
`37fb6a2aab93c1aca0ea19efd4528a8c958b5f97` (2020-09-28).

The upstream repository and complete corresponding source snapshot are
available in `source.tar.gz`. The license is in `LICENSE.txt`.

The class modifier supports the upstream `v1_8_R3` transformations for
`MinecraftServer` and `CraftServer`. Its Java 8-compatible binary was built
with JDK 21; the temporary build used Lombok 1.18.30 because the upstream
Lombok 1.18.8 cannot compile on modern JDKs. No SWM source files were changed.

The SWM plugin itself is not bundled. Place its JAR in `plugins/` before
starting Titanium with `run.sh` or `run.bat`. The class modifier is embedded in
`paperclip.jar`; the launcher detects the plugin descriptor and activates it
before Minecraft classes load. If SWM is absent, the modifier is not loaded and
Titanium starts normally.

Upstream: https://github.com/cijaaimee/Slime-World-Manager
