# Essence & Industry

Minecraft machinery, nuclear power, resource crops, storage utilities and automation.

## Versions

- `26.1.2/` — Minecraft 26.1.2, NeoForge 26.1.2.109, Java 25.

Each Minecraft port lives in its own top-level version folder. This repository contains runtime source, assets, in-game guides and build files. Development captures, test fixtures and prototypes are maintained separately.

## Build

From `26.1.2/`, run `./gradlew build` (Windows: `gradlew.bat build`).

Integration API jars are compile-only dependencies and are not redistributed here. Place the matching Minecraft 26.1.2 mod jars in `26.1.2/libs/`, or pass `-Plocal_mods_dir=/path/to/mods`. The required jar patterns are listed in `build.gradle`; nested AdvancedAE/EnderIO API jars are extracted automatically. Use the matching versions listed in `26.1.2/DEPENDENCIES.md`. Gradle downloads NeoForge and its normal dependencies.

The jar is written to `26.1.2/build/libs/`. Install Mystical Agriculture and Cucumber alongside it; other integrations require their respective mods.

## Releases

Publishing a GitHub Release sends its name and link to Discord through the `DISCORD_RELEASE_WEBHOOK` repository secret. No webhook credentials are stored in source. Drafts and ordinary pushes do not send notifications.

## Compatibility

Runtime migration aliases and conversion recipes preserve existing worlds. They are active compatibility code, not abandoned prototypes.

MIT license; see [credits](CREDITS.md).
