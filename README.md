# Polymer Patch for Guard Villagers

A server-side patch that lets [Guard Villagers](https://modrinth.com/mod/guard-villagers-fabricquilt) run on servers where
players use unmodded clients. Built on [Polymer](https://polymer.pb4.eu).

Install the patch next to Guard Villagers on the server. Nothing is needed on the client.

## What vanilla clients get

- Guard's models and geometry have been rebuilt from the original mod using Polymer's virtual entities
  as well as item displays for each part of the model. Animated by server tick.
- Right-clicking a guard opens its inventory: helmet, chestplate, leggings, boots, offhand and main
  hand in one row, plus the mod's follow and patrol toggles and a readout of its health and armour.
  Every slot does something, and equipping works exactly like the original screen.
- The guard spawn egg appears as a villager spawn egg and spawns a guard.
- Guard sounds fall back to the villager ones, so no custom registry entries reach the client.

## Requirements

- Minecraft 26.1.2, Fabric Loader 0.19.5+
- Fabric API
- [Guard Villagers](https://modrinth.com/mod/guard-villagers-fabricquilt)
- [Polymer](https://modrinth.com/mod/polymer), including `polymer-resource-pack`
- The Polymer resource pack must be served and accepted by clients; the guard skins live in it.
  Clients without it draw the guards with a missing skin texture.

## Limitations

- The GUI is a one-row chest panel: no 3D guard preview, with health and armour shown on the status
  item instead of on bars.

## Building

```
gradle build
```

The jar is written to `build/libs`.

## License

MIT
