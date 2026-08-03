package net.busybee.ddv_fishing.client.hud;

import net.busybee.ddv_fishing.networking.FishingMinigameResultC2SPacket;
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

    private static boolean active = false;
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

    public static void start(int hits, float difficultySpeed) {
        active = true;
        currentHits = 0;
        requiredHits = hits;
        
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
            fail();
        }
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!active) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        var textRenderer = MinecraftClient.getInstance().textRenderer;

        context.drawCenteredTextWithShadow(textRenderer, 
            "Successes: " + currentHits + "/" + requiredHits, centerX, centerY + 100, 0xFFFFFFFF);

        String instruction = isSafeToClick() ? "CLICK NOW!" : "Wait for it...";
        int instructionColor = isSafeToClick() ? 0xFF00FF00 : 0xFFFFFFFF;
        context.drawCenteredTextWithShadow(textRenderer, instruction, centerX, centerY + 115, instructionColor);
    }

    public static void renderWorld(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (!active) return;

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
        if (!active || actionCooldown > 0) return;

        if (isSafeToClick()) {
            boolean isPerfect = Math.abs(ringScale - targetScale) < 0.05f;
            currentHits++;
            ringScale = 2.0f;
            soundTicker = 0;
            chimePlayed = false;
            actionCooldown = 10;
            if (currentHits >= requiredHits) {
                win(isPerfect);
            }
        } else {
            fail();
        }
    }

    private static void win(boolean isPerfect) {
        active = false;
        postMinigameCooldown = 10;
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(true, isPerfect));
    }

    private static void fail() {
        active = false;
        postMinigameCooldown = 10;
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(false, false));
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
