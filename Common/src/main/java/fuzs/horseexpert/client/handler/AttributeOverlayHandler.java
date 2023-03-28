package fuzs.horseexpert.client.handler;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.horseexpert.HorseExpert;
import fuzs.horseexpert.client.gui.screens.inventory.tooltip.ClientHorseAttributeTooltip;
import fuzs.horseexpert.config.ClientConfig;
import fuzs.horseexpert.core.CommonAbstractions;
import fuzs.horseexpert.init.ModRegistry;
import fuzs.horseexpert.world.inventory.tooltip.HorseAttributeTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.Optional;

public class AttributeOverlayHandler {

    public static void renderAttributeOverlay(Minecraft minecraft, PoseStack poseStack, int screenWidth, int screenHeight) {
        isRenderingTooltipsAllowed(minecraft).ifPresent(abstractHorse -> {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            actuallyRenderAttributeOverlay(poseStack, screenWidth, screenHeight, abstractHorse, minecraft.font, minecraft.getItemRenderer());
        });
    }

    private static Optional<AbstractHorse> isRenderingTooltipsAllowed(Minecraft minecraft) {
        if (!minecraft.options.hideGui) {
            Options options = minecraft.options;
            if (options.getCameraType().isFirstPerson() && minecraft.crosshairPickEntity instanceof AbstractHorse animal) {
                if (minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR && minecraft.cameraEntity instanceof Player player && CommonAbstractions.INSTANCE.findEquippedItem(player, ModRegistry.MONOCLE_ITEM.get()).isPresent() && (!HorseExpert.CONFIG.get(ClientConfig.class).requiresSneaking || player.isShiftKeyDown())) {
                    if (!HorseExpert.CONFIG.get(ClientConfig.class).mustBeTamed || animal.isTamed()) {
                        return Optional.of(animal);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static void actuallyRenderAttributeOverlay(PoseStack poseStack, int screenWidth, int screenHeight, AbstractHorse animal, Font font, ItemRenderer itemRenderer) {
        List<HorseAttributeTooltip> tooltipComponents = buildTooltipComponents(animal);
        int posX = screenWidth / 2 - 12 + 22 + HorseExpert.CONFIG.get(ClientConfig.class).offsetX;
        int posY = screenHeight / 2 + 15 - (tooltipComponents.size() * 29 - 3) / 2 + HorseExpert.CONFIG.get(ClientConfig.class).offsetY;
        for (int i = 0; i < tooltipComponents.size(); i++) {
            TooltipRenderHelper.renderTooltip(poseStack, posX, posY + 29 * i, Component.empty(), new ClientHorseAttributeTooltip(tooltipComponents.get(i)));
        }
    }

    private static List<HorseAttributeTooltip> buildTooltipComponents(AbstractHorse animal) {
        List<HorseAttributeTooltip> tooltipComponents = Lists.newArrayList();
        tooltipComponents.add(HorseAttributeTooltip.healthTooltip(animal.getAttributeValue(Attributes.MAX_HEALTH)));
        if (!(animal instanceof Llama) || HorseExpert.CONFIG.get(ClientConfig.class).allLlamaAttributes) {
            tooltipComponents.add(HorseAttributeTooltip.speedTooltip(animal.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            tooltipComponents.add(HorseAttributeTooltip.jumpHeightTooltip(animal.getAttributeValue(Attributes.JUMP_STRENGTH)));
        }
        if (animal instanceof Llama llama) {
            tooltipComponents.add(HorseAttributeTooltip.strengthTooltip(llama.getStrength()));
        }
        return tooltipComponents;
    }
}
