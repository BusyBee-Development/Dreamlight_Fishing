# 🎣 Dreamlight Fishing

Tired of the boring "wait-and-click" vanilla fishing? **Dreamlight Fishing** completely overhauls Minecraft's fishing system, bringing the dynamic, rhythm-based mechanics of *Disney Dreamlight Valley* straight to the Fabric ecosystem.

Say goodbye to staring blankly at a floating stick. Say hello to active water hotspots, skill-based timing, interactive physical reeling, and weather-affected challenges!

---

## ✨ Key Features

*   **🌊 Dynamic Fishing Ripples:** Discover active, bubbling surface disturbances that naturally spawn in nearby open water.
*   **🎯 Rhythm Minigame:** Timing-based shrinking ring minigame featuring visual cues, sound feedback, and perfect-timing rewards.
*   **🎣 Interactive Reeling:** Once hooked, the fish appears in the world and physically resists while you repeatedly reel it toward you. Keep pulling before the line snaps!
*   **⭐ Perfect Catches:** Time your final hit with precision to trigger special visual effects and celebrate your perfect catch.
*   **🌧️ Weather Synergy:** Rain and thunderstorms intensify the minigame, raising the difficulty and making your timing skills truly count.

---

## 🔍 Understanding Fishing Ripples

Ripples naturally appear on exposed water near online players and fade over time. Higher rarities demand faster reflexes and more consecutive hits:

| Tier       | Required Successes   | Minigame Speed   | Challenge Level   |
|:-----------|:---------------------|:-----------------|:------------------|
| **Common** | **2 Hits**           | Slowest          | Relaxed           |
| **Rare**   | **3 Hits**           | Faster           | Challenging       |
| **Epic**   | **4 Hits**           | Fastest          | Ultimate Test     |

*Note: Minigame rarity currently increases timing difficulty. Custom per-tier loot tables will be expanded in future updates.*

---

## 🎮 How to Play

### 1. Spot & Cast
Look for bubbling ripples on nearby water. Cast your bobber directly inside the ripple and wait for a bite.

### 2. Time the Rings
When the minigame appears, watch the outer ring shrink toward the fixed inner circle.
*   **The Action:** Press **Use (Right-Click)** or **Attack** when the circles overlap.
*   **Repeat:** Successfully complete all required hits for that ripple's rarity.

### 3. Reel It In!
Once hooked, the fish appears in the water!
*   Repeatedly use your rod to pull the fish toward you while it struggles and resists.
*   Don't let it get too far, or the line will snap!

---

## ⚙️ Installation & Requirements

| Property                    | Requirement                          |
|:----------------------------|:-------------------------------------|
| **Mod Loader**              | Fabric                               |
| **Java Version**            | Java 21                              |
| **Supported Game Versions** | Minecraft `1.21.1` through `1.21.11` |
| **Dependencies**            | Fabric API                           |

> **⚠️ Important Version Notice:**  
> Download the specific build built for your **exact** Minecraft release. Builds are not cross-compatible (e.g., a `1.21.11` jar will not load on a `1.21.1` server or client).

---

## 🖥️ Server Owner Notes
**Note:** Server side is still in Beta.

*   **Dual Installation:** Required on both **Server** and **Client**. The client provides the minigame UI, rendering, input handling, and audio.
*   **Server Authority:** Minigame outcomes, physical reeling physics, rod durability wear, line snapping, and loot delivery are fully managed server-side.
*   **Vanilla Replacement:** Replaces standard vanilla fishing while installed; fishing is centered around generated ripples.
*   **Current Defaults:** Config files are not yet available; ripple spawn rates, rarity chances, reeling tension, and loot tables use built-in defaults.

---

## 📌 Current Limitations & Future Roadmap

*   **Loot:** Currently awards the four vanilla fish items (Cod, Salmon, Tropical Fish, Pufferfish).
*   **Integrations:** No config file, custom loot table hooks, or external modded fish support available yet.
*  **Custom Textures:** We plan on adding in more fish, custom fishing poles, animations and sounds in the future as well.

---

## 🤝 Want to Contribute?

**Dreamlight Fishing** is an evolving project, and contributions, feedback, and ideas from the community are always welcome! Whether you are a programmer, artist, translator, or player, here is how you can help move the project forward:

### 🛠️ Ways You Can Help
*   **🐛 Report Bugs:** Found an issue, crash, or unexpected behavior? Open an issue on our tracker with your game version, mod list, and crash logs.
*   **💡 Feature Ideas:** Have a suggestion for future updates (like config options, custom loot tables, or mod integrations)? We'd love to hear it!
*   **💻 Code & Pull Requests:** Feel free to fork the repository and submit PRs for bug fixes, code optimizations, or new features.
*   **🎨 Assets & Audio:** Sound design and visual FX contributions to make the minigame feel even smoother are always appreciated.

### 📜 Getting Started
1. **Fork & Clone:** Fork the repository and clone it to your local environment.
2. **Environment:** Ensure you are set up with **Java 21** and a Fabric-compatible IDE (IntelliJ IDEA recommended).
3. **Submit a PR:** Keep changes focused, follow standard Java conventions, and test thoroughly across supported Minecraft versions before submitting!

*For quick discussions, dev builds, and support tickets, join our **[Discord Server](https://discord.com/invite/abdm29q7af)**!*

---

## 👤 Credits

Developed by **BusyBee**.  
*Inspired by the fishing mechanics in Disney Dreamlight Valley.*