# BHAPI (Beta Horizons API)
[![](https://jitpack.io/v/paulevsGitch/BHAPI.svg)](https://jitpack.io/#paulevsGitch/BHAPI)

Simple API for Minecraft Beta 1.7.3 build on top of Babric (Fabric fork for Beta 1.7.3).

API uses [BH BIN Mappings](https://github.com/paulevsGitch/BIN-Mappings-BH).

Documentation is not available yet, but there are javadocs in the code.

### Features:
- Custom world save format (WIP)
- Worlds with large heights
- Registries
- Blockstates
- Custom Blocks
- Custom Items
- Custom Commands
- Events (only startup, 3 types)
- Configs
- Multithreading (Configurable)

### Fixes:
- Custom proxy ([Betacraft](https://betacraft.uk/)), should fix missing sounds (Configurable)
- Custom skins server ([Minotar](https://minotar.net)), fix missing skins (Configurable)
- Fixed startup errors related to outdated controllers code and missing library path

### Planned Things:
- Texture atlas (will use [CoreLib](https://github.com/paulevsGitch/B.1.7.3-CoreLib) implementation)
- Custom block rendering and models (Vanilla, Modern, OBJ and procedural)
- Dimensions API
- Fuel API (partially implemented)
- Tags (postponed)
- Partial Minecraft code optimisations (where it is possible)

### Subjective Features:
- Authentication fix (working online servers)

If you want to suggest any feature you can use this GitHub [issue tracker](https://github.com/paulevsGitch/BHAPI/issues)
or Beta Horizons [Discord server](https://discord.gg/qxcP8EjkUC).