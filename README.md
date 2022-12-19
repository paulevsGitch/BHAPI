# BHAPI (Beta Horizons API)
[![](https://jitpack.io/v/paulevsGitch/BHAPI.svg)](https://jitpack.io/#paulevsGitch/BHAPI)

Simple API for Minecraft Beta 1.7.3 build on top of Babric (Fabric fork for Beta 1.7.3).

API uses [BH BIN Mappings](https://github.com/paulevsGitch/BIN-Mappings-BH).

Documentation is not available yet, but there are javadocs in the code.

**[Basic Mod Template](https://github.com/paulevsGitch/BHAPI-Mod-Template)**

### Features:
- Custom world save format (WIP)
- Worlds with large heights
- Registries for blocks, items, events
- Blockstates and Blockstate Properties
- Custom blocks
- Custom items
- Custom commands
- Custom recipes
- Custom textures
- Animations (using .mcmeta format)
- Events (only startup and on resource reloading)
- Configs (JSON format)
- Multithreading (Configurable)
- Texture atlas (for blocks, items and particles)
- Custom block rendering and models (Vanilla, Modern JSON, OBJ and procedural)
- Custom world rendering
- Custom light spreading
- Automatic texture render layers (instead of hardcoding in blocks)
- Translations

### Fixes:
- Custom proxy ([Betacraft](https://betacraft.uk/)), should fix missing sounds (Configurable)
- Custom skins server ([Minotar](https://minotar.net)), fix missing skins (Configurable)
- Fixed startup errors related to outdated controllers code and missing library path

### Planned Things:
- Dimensions API
- Fuel API (partially implemented)
- Tags (postponed)
- Partial Minecraft code optimisations (where it is possible)

### Subjective Features:
- Authentication fix (working online servers)

If you want to suggest any feature you can use this GitHub [issue tracker](https://github.com/paulevsGitch/BHAPI/issues)
or Beta Horizons [Discord server](https://discord.gg/qxcP8EjkUC).