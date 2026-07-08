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
    private static final Identifier TARGET_TEXTURE = Identifier.of("minecraft", "textures/particle/bubble.png");
    private static final Identifier RING_TEXTURE = Identifier.of("minecraft", "textures/particle/bubble.png");

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
        
        // Feature A: Dynamic Audio Feedback
        float diff = Math.abs(ringScale - targetScale);
        if (ringScale > targetScale && diff < 0.6f && diff > 0.12f) {
            int soundInterval = (int) (diff * 15);
            if (soundTicker <= 0) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null && client.world != null) {
                    float volume = 0.2f + (0.6f - diff) * 0.4f;
                    float pitch = 1.2f + (0.6f - diff) * 0.8f;
                    client.world.playSound(client.player, client.player.getBlockPos(), 
                        SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.PLAYERS, volume * 0.5f, pitch);
                    client.world.playSound(client.player, client.player.getBlockPos(), 
                        SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, SoundCategory.PLAYERS, volume, pitch + 0.5f);
                }
                soundTicker = Math.max(1, soundInterval);
            }
        } else if (isSafeToClick() && !chimePlayed) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                client.world.playSound(client.player, client.player.getBlockPos(), 
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
        matrices.translate(0, 0.1, 0);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(90));

        renderWorldRing(matrices, vertexConsumers, targetScale, 0.0f, 1.0f, 0.0f, 0.4f, TARGET_TEXTURE);

        float r = 1.0f, g = 1.0f, b = 1.0f;
        if (Math.abs(currentScale - targetScale) > 0.12f) {
            r = 1.0f; g = 0.2f; b = 0.2f; // Reddish if outside
        }
        renderWorldRing(matrices, vertexConsumers, currentScale, r, g, b, 0.8f, RING_TEXTURE);
        
        matrices.pop();
    }

    private static void renderWorldRing(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.client.render.VertexConsumerProvider vertexConsumers, float scale, float r, float g, float b, float a, Identifier texture) {
        net.minecraft.client.render.VertexConsumer buffer = vertexConsumers.getBuffer(net.minecraft.client.render.RenderLayer.getEntityTranslucent(texture));
        float size = scale * 1.5f; // Adjust size for world-space
        
        net.minecraft.client.util.math.MatrixStack.Entry entry = matrices.peek();
        drawQuad(buffer, entry, -size, -size, size, size, r, g, b, a);
    }

    private static void drawQuad(net.minecraft.client.render.VertexConsumer buffer, net.minecraft.client.util.math.MatrixStack.Entry entry, float minX, float minY, float maxX, float maxY, float r, float g, float b, float a) {
        buffer.vertex(entry.getPositionMatrix(), minX, minY, 0).color(r, g, b, a).texture(0, 0).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), minX, maxY, 0).color(r, g, b, a).texture(0, 1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), maxX, maxY, 0).color(r, g, b, a).texture(1, 1).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0, 0, 1);
        buffer.vertex(entry.getPositionMatrix(), maxX, minY, 0).color(r, g, b, a).texture(1, 0).overlay(net.minecraft.client.render.OverlayTexture.DEFAULT_UV).light(0xF000F0).normal(entry, 0, 0, 1);
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
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(true, isPerfect));
    }

    private static void fail() {
        active = false;
        ClientPlayNetworking.send(new FishingMinigameResultC2SPacket(false, false));
    }

    public static boolean isActive() {
        return active;
    }

    public static float getTargetDiff() {
        return Math.abs(ringScale - targetScale);
    }
}
