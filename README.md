# Polymer Patch for Guard Villagers

A server-side patch that lets [Guard Villagers](https://modrinth.com/mod/guard-villagers) run on servers where
players use unmodded clients. Built on [Polymer](https://polymer.pb4.eu).

Install the patch next to Guard Villagers on the server. Nothing is needed on the client.

## What vanilla clients get

- Guards are shown as mannequins wearing the mod's own guard textures, remapped from its guard atlas
  to the player skin layout, so they keep the guard face, hat and tunic colours per variant instead
  of looking like Steve. Armour, shields and weapons the guard carries render on it like they would
  on a player, so you can tell what it is wearing and holding.
- Right-clicking a guard opens its inventory: helmet, chestplate, leggings, boots, offhand and main
  hand in one row, plus the mod's follow and patrol toggles and a readout of its health and armour.
  Every slot does something, and equipping works exactly like the original screen.
- The guard spawn egg appears as a villager spawn egg and spawns a guard.
- Guard sounds fall back to the villager ones, so no custom registry entries reach the client.

## Requirements

- Minecraft 26.1.2, Fabric Loader 0.19.5+
- Fabric API
- [Guard Villagers](https://modrinth.com/mod/guard-villagers) 2.1.3+
- [Polymer](https://modrinth.com/mod/polymer), including `polymer-resource-pack`
- The Polymer resource pack must be served and accepted by clients; the guard skins live in it.
  Clients without it draw the guards with a missing skin texture.

## Limitations

- A vanilla client can only draw entities and geometry it already knows. The mod's guard model (a
  taller head with a nose, a hat, shoulder pads and a quiver) cannot be sent to it, so guards use
  the mannequin's player-shaped body. The hat and nose are painted into the skin; the modelled
  shoulder pads and quiver cannot be shown, and the body is player shaped rather than villager
  shaped.
- The GUI is a one-row chest panel: no 3D guard preview, with health and armour shown on the status
  item instead of on bars.

## Building

```
gradle build
```

The jar is written to `build/libs`.

The guard skins in `src/main/resources/assets/guardvillagerspatch/textures/entity/guard/` are
generated from Guard Villagers' own guard atlas with `python3 tools/convert_guard_skins.py`
(needs Pillow). Previews of the result are written to `build/guard-skin-previews/`.

## License

MIT
