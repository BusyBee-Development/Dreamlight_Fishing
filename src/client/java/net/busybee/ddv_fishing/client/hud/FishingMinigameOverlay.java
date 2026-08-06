package net.busybee.ddv_fishing.client.hud;

import net.busybee.ddv_fishing.networking.FishingMinigameResultC2SPacket;
import net.busybee.ddv_fishing.networking.FishingMinigamePhaseC2SPacket;
import net.busybee.ddv_fishing.networking.MinigameResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class FishingMinigameOverlay {
    private static final Identifier WHITE_TEXTURE = Identifier.of("minecraft", "textures/block/white_concrete.png");
    private static final int RING_SEGMENTS = 64;

    public enum Phase {
        TIMING,
        REELING
    }

    private static boolean active = false;
    private static Phase phase = Phase.TIMING;
    private static float ringScale = 2.0f;
    private static float prevRingScale = 2.0f;
    private static float targetScale = 0.5f;
    private static int requiredHits = 3;
    private static int currentHits = 0;
    private static float speed = 0.02f;
    private static int actionCooldown = 0;
    private static int soundTicker = 0;
    private static int swingTicker = 0;
    private static boolean chimePlayed = false;
    private static int postMinigameCooldown = 0;

    private static float tension = 0.0f;
    private static float reelProgress = 0.0f;
    private static int fishRarity = 0;
    private static boolean isPerfect = true;
    private static MinigameResult lastResult = MinigameResult.SUCCESS;

    public static void start(int hits, float difficultySpeed, int rarity) {
        active = true;
        phase = Phase.TIMING;
        currentHits = 0;
        requiredHits = hits;
        fishRarity = rarity;
        tension = 0.0f;
        reelProgress = 0.25f; // Start at 25% progress
        isPerfect = true;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.isRaining()) {
            difficultySpeed *= 1.2f;
        }
        
        speed = difficultySpeed;
        ringScale = 2.0f;
        prevRingScale = 2.0f;
        actionCooldown = 0;
        soundTicker = 0;
        swingTicker = 0;
        chimePlayed = false;
    }

    public static void stop() {
        active = false;
    }

    public static void tick() {
        if (postMinigameCooldown > 0) {
            postMinigameCooldown--;
        }

        if (!active) return;

        if (phase == Phase.TIMING) {
            tickTiming();
        } else {
            tickReeling();
        }
    }

    private static void tickTiming() {
        if (actionCooldown > 0) {
            actionCooldown--;
        }

        prevRingScale = ringScale;
        ringScale -= speed;

        if (swingTicker <= 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.swingHand(Hand.MAIN_HAND);
            }
            swingTicker = 12;
        }
        if (swingTicker > 0) swingTicker--;
        
        float diff = Math.abs(ringScale - targetScale);
        if (ringScale > targetScale && diff < 0.6f && diff > 0.12f) {
            int soundInterval = (int) (diff * 15);
            if (soundTicker <= 0) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && client.world != null) {
                    float volume = 0.2f + (0.6f - diff) * 0.4f;
                    float pitch = 1.2f + (0.6f - diff) * 0.8f;
                    client.world.playSound(null, client.player.getBlockPos(), 
                        SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.PLAYERS, volume * 0.5f, pitch);
                    client.world.playSound(null, client.player.getBlockPos(), 
                        SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, volume, pitch + 0.5f);
                }
                soundTicker = Math.max(1, soundInterval);
            }
        } else if (isSafeToClick() && !chimePlayed) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                client.world.playSound(null, client.player.getBlockPos(), 
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.6f, 2.0f);
            }
            chimePlayed = true;
        }
        
        if (soundTicker > 0) soundTicker--;

        if (ringScale < 0.2f) {
            fail(MinigameResult.ESCAPE);
        }
    }

    private static void tickReeling() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean isReeling = client.options.useKey.isPressed();
        
        float resistance = 0.004f + (fishRarity * 0.003f);
        float tensionGain = 0.015f + (fishRarity * 0.005f);
        float progressGain = 0.012f - (fishRarity * 0.002f);
        
        if (isReeling) {
            tension += tensionGain;
            reelProgress += progressGain;
            
            if (swingTicker <= 0) {
                if (client.player != null) client.player.swingHand(Hand.MAIN_HAND);
                swingTicker = 6;
            }
        } else {
            tension -= 0.02f;
            reelProgress -= resistance;
        }
        
        tension = MathHelper.clamp(tension, 0.0f, 1.1f);
        reelProgress = MathHelper.clamp(reelProgress, -0.1f, 1.1f);
        
        if (swingTicker > 0) swingTicker--;
        
        if (tension >= 1.0f) {
            fail(MinigameResult.SNAP);
        } else if (reelProgress <= 0.0f) {
            fail(MinigameResult.ESCAPE);
        } else if (reelProgress >= 1.0f) {
            win(isPerfect);
        }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        var textRenderer = MinecraftClient.getInstance().textRenderer;

        if (active) {
            if (phase == Phase.TIMING) {
                context.drawCenteredTextWithShadow(textRenderer, 
                    "Successes: " + currentHits + "/" + requiredHits, centerX, centerY + 100, 0xFFFFFFFF);

                String instruction = isSafeToClick() ? "CLICK NOW!" : "Wait for it...";
                int instructionColor = isSafeToClick() ? 0xFF00FF00 : 0xFFFFFFFF;
                context.drawCenteredTextWithShadow(textRenderer, instruction, centerX, centerY + 115, instructionColor);
            } else {
                renderReeling(context, centerX, screenHeight, textRenderer);
            }
        } else if (postMinigameCooldown > 0) {
            String message = "";
            int color = 0xFFFFFFFF;
            if (lastResult == MinigameResult.SNAP) {
                message = "LINE SNAPPED!";
                color = 0xFFFF0000;
            } else if (lastResult == MinigameResult.ESCAPE) {
                message = "FISH GOT AWAY...";
                color = 0xFFAAAAAA;
            } else if (lastResult == MinigameResult.SUCCESS) {
                message = isPerfect ? "PERFECT CATCH!" : "FISH CAUGHT!";
                color = 0xFF00FF00;
            }
            
            if (!message.isEmpty()) {
                context.drawCenteredTextWithShadow(textRenderer, message, centerX, centerY, color);
            }
        }
    }

    private static void renderReeling(DrawContext context, int centerX, int screenHeight, net.minecraft.client.font.TextRenderer textRenderer) {
        int barWidth = 4;
        int barHeight = 40;
        int yBottom = screenHeight - 40;
        int yTop = yBottom - barHeight;

        // Tension Bar (Left of Health)
        int xTension = centerX - 91 - 10;
        context.fill(xTension - 1, yTop - 1, xTension + barWidth + 1, yBottom + 1, 0xFF000000);
        
        int tensionColor = 0xFF00FF00;
        if (tension > 0.8f) tensionColor = 0xFFFF0000;
        else if (tension > 0.5f) tensionColor = 0xFFFFFF00;
        
        int tensionFillHeight = (int)(barHeight * MathHelper.clamp(tension, 0.0f, 1.0f));
        context.fill(xTension, yBottom - tensionFillHeight, xTension + barWidth, yBottom, tensionColor);
        context.drawCenteredTextWithShadow(textRenderer, "T", xTension + barWidth / 2, yTop - 12, 0xFFFFFFFF);

        // Progress Bar (Right of Hunger)
        int xProgress = centerX + 91 + 6;
        context.fill(xProgress - 1, yTop - 1, xProgress + barWidth + 1, yBottom + 1, 0xFF000000);
        
        int progressFillHeight = (int)(barHeight * MathHelper.clamp(reelProgress, 0.0f, 1.0f));
        context.fill(xProgress, yBottom - progressFillHeight, xProgress + barWidth, yBottom, 0xFF00AAFF);
        context.drawCenteredTextWithShadow(textRenderer, "P", xProgress + barWidth / 2, yTop - 12, 0xFFFFFFFF);

        String instruction = MinecraftClient.getInstance().options.useKey.isPressed() ? "REELING!" : "HOLD [USE] TO REEL";
        context.drawCenteredTextWithShadow(textRenderer, instruction, centerX, screenHeight - 55, 0xFFCCCCCC);
    }

    public static void renderWorld(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (!active || phase != Phase.TIMING) return;

        float currentScale = MathHelper.lerp(tickDelta, prevRingScale, ringScale);
        
        matrices.push();
        matrices.translate(0, 0.05, 0); // Slightly above water
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90));

        // Target Ring (Gold/Yellow)
        renderWorldRing(matrices, vertexConsumers, targetScale, 1.0f, 0.84f, 0.0f, 0.8f);

        // Closing Ring (Green if safe, Red otherwise)
        float r, g, b;
        if (isSafeToClick()) {
            r = 0.0f; g = 1.0f; b = 0.0f;
        } else {
            r = 1.0f; g = 0.2f; b = 0.2f;
        }
        renderWorldRing(matrices, vertexConsumers, currentScale, r, g, b, 0.9f);
        
        matrices.pop();
    }

    private static void renderWorldRing(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, float scale, float r, float g, float b, float a) {
        //? if >=1.21.11 {
        net.minecraft.client.render.VertexConsumer buffer = vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayers.entityTranslucent(WHITE_TEXTURE));
        //?} else {
        /*net.minecraft.client.render.VertexConsumer buffer = vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityTranslucent(WHITE_TEXTURE));
        *///?}
        float outerRadius = scale; 
        float innerRadius = Math.max(0.0f, outerRadius - 0.1f); // Thicker ring
        net.minecraft.client.util.math.MatrixStack.Entry entry = matrices.peek();
        for (int segment = 0; segment < RING_SEGMENTS; segment++) {
            double angle0 = Math.PI * 2.0 * segment / RING_SEGMENTS;
            double angle1 = Math.PI * 2.0 * (segment + 1) / RING_SEGMENTS;
            drawRingSegment(buffer, entry,
                    (float) Math.cos(angle0) * innerRadius, (float) Math.sin(angle0) * innerRadius,
                    (float) Math.cos(angle0) * outerRadius, (float) Math.sin(angle0) * outerRadius,
                    (float) Math.cos(angle1) * outerRadius, (float) Math.sin(angle1) * outerRadius,
                    (float) Math.cos(angle1) * innerRadius, (float) Math.sin(angle1) * innerRadius,
                    r, g, b, a);
        }
    }

    private static void drawRingSegment(net.minecraft.client.render.VertexConsumer buffer, net.minecraft.client.util.math.MatrixStack.Entry entry,
                                        float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                                        float r, float g, float b, float a) {
        int overlay = net.minecraft.client.render.OverlayTexture.DEFAULT_UV;
        buffer.vertex(entry.getPositionMatrix(), x0, y0, 0).color(r, g, b, a).texture(0, 0).overlay(overlay).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), x1, y1, 0).color(r, g, b, a).texture(0, 1).overlay(overlay).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), x2, y2, 0).color(r, g, b, a).texture(1, 1).overlay(overlay).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), x3, y3, 0).color(r, g, b, a).texture(1, 0).overlay(overlay).light(0xF000F0).normal(entry, 0, 0, 1);
    }

    public static boolean isSafeToClick() {
        float margin = 0.12f;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.isThundering()) {
            margin = 0.08f;
        }
        return Math.abs(ringScale - targetScale) < margin;
    }

    public static void onAction() {
        if (!active || actionCooldown > 0 || phase != Phase.TIMING) return;

        if (isSafeToClick()) {
            boolean currentPerfect = Math.abs(ringScale - targetScale) < 0.05f;
            if (!currentPerfect) isPerfect = false;
            
            currentHits++;
            ringScale = 2.0f;
            soundTicker = 0;
            chimePlayed = false;
            actionCooldown = 10;
            if (currentHits >= requiredHits) {
                phase = Phase.REELING;
                ClientPlayNetworking.send(new FishingMinigamePhaseC2SPacket(2));
            }
        } else {
            fail(MinigameResult.ESCAPE);
        }
    }

    private static void win(boolean isPerfect) {
        active = false;
        lastResult = MinigameResult.SUCCESS;
        postMinigameCooldown = 40;
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(MinigameResult.SUCCESS, isPerfect));
    }

    private static void fail(MinigameResult result) {
        active = false;
        lastResult = result;
        postMinigameCooldown = 40;
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(result, false));
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isRecentlyActive() {
        return active || postMinigameCooldown > 0;
    }

    public static float getTargetDiff() {
        return Math.abs(ringScale - targetScale);
    }
}
