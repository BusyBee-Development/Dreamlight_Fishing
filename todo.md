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
- [x] **Extract shared biome classification**
    - [x] Pull `isOcean`/`isSwamp`/`isJungle` out of `FishingRippleEntity.spawnParticles()` into a static `FishBiome.classify(ServerWorld, BlockPos)` helper (`OCEAN`/`SWAMP`/`JUNGLE`/`OTHER`)
    - [x] Update `FishingRippleEntity` to use the helper instead of its inline checks (no behavior change)
- [x] **Wire biome into loot selection**
    - [x] `FishingLootHandler.generateLoot()` classifies the bobber's block pos with `FishBiome` and picks `{biome}_{rarity}` when biome != OTHER, else falls back to the existing generic `common`/`rare`/`epic` tables
- [x] **Add custom fish as ripple-tier rewards (placeholder art)**
    - [x] Register new items in `ModItems`: Ocean `ocean_pearl` (rare), `large_fish` (epic); Swamp `algae` (common), `catfish` (rare); Jungle `exotic_fish` (rare), `river_piranha` (epic)
    - [x] Generated-item model + flat placeholder texture per item, swappable later
    - [x] `en_us.json` lang entries for the new item names
- [x] **Author biome loot tables**
    - [x] Create `gameplay/fishing/{ocean,swamp,jungle}_{common,rare,epic}.json` (9 tables), reweighting vanilla fish per biome and layering in the new custom items
    - [x] Leave `common.json`/`rare.json`/`epic.json` as the untouched `OTHER`-biome fallback
- [x] **Give perfect catches more exciting rewards**
    - [x] In `FishingLootHandler.catchFish()`, beyond double loot: ~15% chance of bonus XP, else ~10% chance of a short `Luck` status effect (mutually exclusive, ~25% combined)

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
