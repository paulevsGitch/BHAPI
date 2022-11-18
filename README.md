# BHAPI (Beta Horizons API)
[![](https://jitpack.io/v/paulevsGitch/BHAPI.svg)](https://jitpack.io/#paulevsGitch/BHAPI)

Simple API for Minecraft Beta 1.7.3 build on top of Babric (Fabric fork for Beta 1.7.3).

API uses [BH BIN Mappings](https://github.com/paulevsGitch/BIN-Mappings-BH).

Documentation is not available yet, but there are javadocs in the code.
**[Basic Mod Template](https://github.com/paulevsGitch/BHAPI-Mod-Template)**

### Features:
- Custom world save format (WIP)
- Worlds with large heights
- Registries
- Blockstates
- Custom blocks
- Custom items
- Custom commands
- Custom recipes
- Events (only startup and on resource reloading)
- Configs
- Multithreading (Configurable)
- Texture atlas (for blocks, items and particles)

### Fixes:
- Custom proxy ([Betacraft](https://betacraft.uk/)), should fix missing sounds (Configurable)
- Custom skins server ([Minotar](https://minotar.net)), fix missing skins (Configurable)
- Fixed startup errors related to outdated controllers code and missing library path

### Planned Things:
- Custom block rendering and models (Vanilla, Modern, OBJ and procedural)
- Dimensions API
- Fuel API (partially implemented)
- Tags (postponed)
- Partial Minecraft code optimisations (where it is possible)

### Subjective Features:
- Authentication fix (working online servers)

If you want to suggest any feature you can use this GitHub [issue tracker](https://github.com/paulevsGitch/BHAPI/issues)
or Beta Horizons [Discord server](https://discord.gg/qxcP8EjkUC).