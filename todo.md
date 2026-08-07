# 📋 Dreamlight Fishing Roadmap

## Phase 1: Completing the Core Loop
- [x] **Build the missing reeling phase**
    - [x] Implement the tension meter HUD
    - [x] Add fish resistance logic (fish pulling away) and the manual reeling interaction
    - [x] Add the "Line Snap" mechanic when tension is too high or the fish gets too far
- [x] **Add better failure feedback**
    - [x] Create visual/text cues for "Fish got away" vs. "Line snapped"
    - [x] Add a small durability penalty for snaps to make the reeling phase meaningful
    - [x] Add basic fishing rod
- [x] **Close the core-loop gaps**
  - [x] Wire up `FishingMinigameOverlay.stop()` — cancels on death, on the bobber
    going away (10-tick grace for tracking dropouts), and on disconnect. Sends
    `MinigameResult.CANCEL` so the server clears `minigameActive` too
  - [x] Move minigame validation server-side; don't trust client-sent
    SUCCESS/isPerfect. Rules shared via `MinigameRules`; each hit reported by
    `FishingMinigameHitC2SPacket` and re-judged against the server's own clock;
    perfect is now the server's verdict and the field is gone from the packet
  - [x] Declare GeckoLib in `fabric.mod.json` depends
  - [x] Scope rod animation state per-stack instead of a global static — resolved
    per render state in the renderer, idle for anything but your own held rod
  - [x] Verify the reeling animation actually plays in game
  - [x] Sit ripple rings flush with the water surface
  - [x] Make `enabled = false` disable the minigame, not just spawning

## Phase 2: Content & Biome Depth
- [ ] **Use existing biome detection for specific catches**
    - [ ] Tie the `isSwamp`, `isJungle`, and `isOcean` checks already in `FishingRippleEntity` to the `FishingLootHandler`
- [ ] **Add custom fish as ripple-tier rewards**
    - [ ] Define unique items for Ocean (Pearls/Large Fish), Swamp (Algae/Catfish), and Jungle (Tropical/Exotic)
    - [ ] Replace vanilla loot in `common.json`, `rare.json`, and `epic.json` with these new entries
- [ ] **Give perfect catches more exciting rewards**
    - [ ] Beyond double loot, add small chances for bonus XP or temporary "Luck" status effects

## Phase 3: Immersion & "Game Juice"
- [ ] **Create custom audio for the entire loop**
    - [ ] Add rising-pitch ticks for the timing ring
    - [ ] Add creaking wood sounds for high tension and a splash for the catch
- [ ] **Polish presentation and accessibility**
    - [ ] Move hardcoded strings to `en_us.json` for localization
    - [ ] Add a config for timing difficulty and HUD scale

## Phase 4: Progression & Systems
- [ ] **Make rods alter the minigame mechanics**
    - [x] `magic rod`: Just the basic rod to replace the vanilla rod
    - [ ] `sturdy rod`: Reduces tension buildup
    - [ ] `swift rod`: Increases reel-in speed
    - [ ] `lucky rod`: Increases rare ripple spawn chance
- [ ] **Add a fishing journal / collection screen**
    - [ ] Create a UI to track "First Catch," "Largest Catch," and biome completion
- [ ] **Add bait and ingredients**
    - [ ] Implement the Seaweed/Red Kale bait system to allow players to target specific biomes or rarities

## Phase 5: Long-term Utility
- [ ] **Prevent ripple spam and balance hotspot selection**
    - [ ] Implement a per-player cap and local cooldowns to make each ripple feel like finding a "node"
- [ ] **Turn fish into cooking, displays, and materials**
    - [ ] Add recipes and trophy mount blocks for legendary catches
