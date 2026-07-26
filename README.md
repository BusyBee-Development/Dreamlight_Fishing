# Dreamlight Fishing

Dreamlight Fishing is a Fabric mod inspired by the fishing minigame from *Disney Dreamlight Valley*. It replaces ordinary fishing with visible fishing ripples, a timing challenge, and an interactive reeling sequence.

## Features

- Fishing ripples appear naturally on exposed water near players.
- Three ripple rarities change the speed and number of successful timings required.
- A two-circle minigame provides visual, sound, and perfect-timing feedback.
- Hooked fish appear in the world and resist while being reeled toward the player.
- Successful catches award cod, salmon, tropical fish, or pufferfish.
- Rain and thunderstorms make the timing challenge harder.

## How to Fish

1. Look for bubbles and surface disturbances in nearby water.
2. Cast your fishing bobber into a fishing ripple and wait for a bite.
3. When the minigame appears, wait for the shrinking outer circle to meet the fixed inner circle.
4. Press **Use/Right-Click** or **Attack** while the circles overlap. Repeat until every required hit is complete.
5. Once the fish is hooked, repeatedly use the fishing rod to pull it toward you. The fish resists between pulls, and the line snaps if it gets too far away.

An accurately timed final hit counts as a perfect catch and produces additional visual effects.

## Ripple Rarities

- **Common:** Two successful timings at the slowest speed.
- **Rare:** Three successful timings at a faster speed.
- **Epic:** Four successful timings at the fastest speed.

Rarity currently changes minigame difficulty. It does not yet provide a separate loot table.

## Installation

- Requires the Fabric mod loader, Fabric API, and Java 21.
- Install the Dreamlight Fishing file made specifically for your exact Minecraft version.
- Supported versions are Minecraft **1.21.1 through 1.21.11**.
- Install the mod and Fabric API on both the server and every joining player. The client installation supplies the minigame interface, input handling, sounds, and rendering.

Do not use a file for a different Minecraft release. For example, the `1.21.11` mod file will not load on a `1.21.1` game or server.

## Server Owner Notes

- Fishing ripples are created automatically near online players and disappear naturally over time.
- The minigame result, reeling simulation, rod damage, line snapping, and catch rewards are controlled by the server.
- The mod currently has no configuration file. Ripple frequency, rarity chances, reeling behavior, and loot chances use built-in defaults.
- Vanilla fishing behavior is replaced while the mod is installed; fishing is intended to take place at generated ripples.
- Removing or reloading the mod while a player is actively reeling may cancel that catch.

## Current Limitations

- Only the four vanilla fish items are available as rewards.
- Ripple rarity does not currently affect which fish is caught.
- Spawn rates, difficulty, loot, and reeling values cannot yet be configured by server owners.
- There are no integrations with custom fishing loot tables or fish added by other mods yet.

## Credits

Developed by **BusyBee**. Inspired by the fishing mechanics in *Disney Dreamlight Valley*.
