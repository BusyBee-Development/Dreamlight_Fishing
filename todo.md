# 📋 Dreamlight Fishing Roadmap

Reordered for a singleplayer-first priority, with server features layered on top of
systems that already exist for solo play.

## Phase 1: Completing the Core Loop — ✅ DONE
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

## Phase 2: Content & Biome Depth — ✅ DONE
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

---

## Phase 3: Journal, Progression & Retention

*This is the phase that turns "fun minigame" into "reason to keep playing." Biome
loot has landed — new fish need somewhere to be tracked.*

- [ ] **Build the fishing journal UI**
  - [ ] Design the screen layout: species list, per-species "caught/not caught,"
    largest catch on record, first-catch date
  - [ ] Populate entries dynamically from the loot table registry so new fish
    (from Phase 2 work) auto-appear without manual UI updates
  - [ ] Add biome completion tracking (e.g. "Ocean: 4/7 species caught")
  - [ ] Persist journal data per-player in save data (and per-player on servers,
    not shared/global)
- [ ] **Add catch metadata: size/weight**
  - [ ] Give each fish species a min-max weight/length range
  - [ ] Roll size at catch time; bias larger rolls on Perfect Catches
  - [ ] Track "largest catch" per species in the journal
  - [ ] Surface size in the catch notification/HUD popup
- [ ] **Tie journal completion to unlocks**
  - [ ] Define a capstone reward for full biome completion (legendary ripple
    tier access, a unique cosmetic rod skin, or a craftable-only-after-unlock item)
  - [ ] Define smaller milestone rewards at 25%/50%/75% completion so progress
    feels continuous, not all-or-nothing
- [ ] **Wire up vanilla advancement integration**
  - [ ] Advancement for first catch, first Perfect Catch, first Epic ripple catch
  - [ ] Advancement for full journal completion per biome
  - [ ] Use vanilla advancement toast/criteria system so it shows up in the
    normal advancements screen, not a custom one

---

## Phase 4: Rod & Bait Progression

*Depth for players who've finished the journal grind and want build variety.*

- [ ] **Finish rod variants**
  - [x] `magic rod`: Just the basic rod to replace the vanilla rod
  - [ ] `sturdy rod`: reduces tension buildup rate — define exact multiplier,
    balance against fish resistance curve
  - [ ] `swift rod`: increases reel-in speed — balance so it doesn't trivialize
    the tension mechanic entirely
  - [ ] `lucky rod`: increases rare/epic ripple spawn chance — define spawn
    weight delta, test against Phase 6 anti-farm cooldown so it doesn't break balance
  - [ ] Decide if rods are crafted, found as loot, or both — write the acquisition
    path before implementing drop tables
- [ ] **Bait system**
  - [ ] Implement Seaweed/Red Kale (and any additional bait types) as consumable
    items
  - [ ] Define what each bait does precisely: biome targeting, rarity boost, or
    species targeting — pick one mechanic per bait type to keep it legible
  - [ ] Add bait consumption on cast, with a HUD indicator for "active bait"
  - [ ] Add bait crafting/farming recipes so it's a renewable resource loop, not
    a one-time find
- [ ] **Rod + bait synergy pass**
  - [ ] Playtest combinations (e.g. lucky rod + rarity bait) to check they don't
    stack multiplicatively into broken rates

---

## Phase 5: Presentation & Polish

- [ ] **Custom audio for the full loop**
  - [ ] Rising-pitch ticks for the timing ring (tie pitch to tension level for
    audio feedback, not just visual)
  - [ ] Creaking wood sound at high tension
  - [ ] Splash sound on catch, distinct sound for Perfect Catch
  - [ ] Distinct sound for line snap vs. fish escape (matches the visual/text
    cue split from Phase 1)
- [ ] **Config and accessibility**
  - [ ] Config for timing difficulty (affects tension window sizes / tick speed)
  - [ ] Config for HUD scale
  - [ ] Colorblind-friendly option for the tension meter / timing ring (don't
    rely on red/green alone)
- [ ] **Localization**
  - [ ] Move all hardcoded strings to `en_us.json`
  - [ ] Structure keys so community translations are easy to contribute

---

## Phase 6: Long-Term Utility & Server Layer

*Server features go last because they're most valuable once journal, rods, and
bait already exist to build on top of — but the anti-farm item below should slide
earlier if biome loot testing reveals exploit potential.*

- [ ] **Anti-farm / ripple balance** *(pull this forward immediately if biome loot
  testing shows ripple camping is viable)*
  - [ ] Per-player ripple spawn cap
  - [ ] Local cooldown between ripple spawns near a given player
  - [ ] Test against `lucky rod` from Phase 4 so the two systems don't fight
- [ ] **Fish as crafting/utility items**
  - [ ] Cooking recipes for new biome fish
  - [ ] Trophy mount display blocks for legendary/largest catches — hook into
    the journal's "largest catch" data
- [ ] **Server-facing systems**
  - [ ] Permission nodes (LuckPerms-compatible) for rare ripple access, bait
    tiers, or specific rod unlocks
  - [ ] `/fishing top` or similar leaderboard command, server-scoped, built on
    top of the same journal/size data already tracked per-player
  - [ ] Vault (or equivalent) economy hook — emit events on catch/sale that
    other plugins can listen to
  - [ ] Document all of the above clearly since server admins won't install
    what they can't configure
