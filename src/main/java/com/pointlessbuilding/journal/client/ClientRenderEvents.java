package com.pointlessbuilding.journal.client;

import java.io.IOException;

import org.joml.Vector3d;
import org.joml.Vector4d;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.items.BuildersCompass;
import com.pointlessbuilding.journal.utility.BoundaryRenderer;
import com.pointlessbuilding.journal.utility.MultiPostChain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRenderEvents {
    
    public static final Logger LOGGER = LogUtils.getLogger();
    private static ResourceLocation dummyLocation = new ResourceLocation(BuildingJournal.MODID,"textures/misc/dummy.png");
    private static MultiPostChain multiPostChain;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {

        checkResizeEvent();

        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
            renderCompassBoundaries(event);
        else if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER)
            renderPostShaderEffects();
        else
            return;
    }

    protected static void checkResizeEvent() {
        if(multiPostChain == null) return;
        if(multiPostChain.screenWidth != Minecraft.getInstance().getWindow().getWidth() || 
            multiPostChain.screenHeight != Minecraft.getInstance().getWindow().getHeight()) {
            multiPostChain.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
        }
    }

    protected static void renderCompassBoundaries(RenderLevelStageEvent event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        ItemStack held = player.getMainHandItem();
        
        if(!BuildersCompass.currentHoldingActiveCompass(player)) return;

        PoseStack ms = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.lines());
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        // Start of draw logic
        int[] first = held.getTag().getIntArray("FirstPos");
        Vector3d firstPos = new Vector3d(first[0], first[1], first[2]);
        Vector3d secondPos;
        if(Minecraft.getInstance().hitResult instanceof BlockHitResult blockHit) {
            secondPos = new Vector3d(blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
        }
        else {
            secondPos = firstPos;
        }

        //render red cuboid
        BoundaryRenderer.renderCuboid(ms, lineConsumer, camera, firstPos, secondPos, new Vector4d(17,131,165,255));
        bufferSource.endBatch(RenderType.lines());

        VertexConsumer faceConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(dummyLocation));
        //render translucent faces
        //BoundaryRenderer.renderCuboidFaces(ms, faceConsumer, camera, firstPos, secondPos, new Vector4d(93,215,251,128), true);
        bufferSource.endBatch(RenderType.entityTranslucent(dummyLocation));
    }

    protected static void renderPostShaderEffects() {
        Minecraft mc = Minecraft.getInstance();
        if(mc.player == null) return;

        boolean applyShaderEffects = BuildersCompass.currentHoldingActiveCompass(mc.player);

        if(applyShaderEffects && multiPostChain == null) {
            try {
            multiPostChain = new MultiPostChain(mc.getResourceManager(), mc.getMainRenderTarget());
            }
            catch (IOException e) {
                LOGGER.error("Failed to load MultiPostChain: {}", e.getMessage());
                multiPostChain = null;
            }
        }
        else if(!applyShaderEffects && multiPostChain != null) {
            multiPostChain.close();
            multiPostChain = null;
        }

        if(multiPostChain == null) return;

        checkResizeEvent();

        RenderTarget maskTarget = multiPostChain.getTempTarget("mask");
        if(maskTarget == null) return;

        maskTarget.bindWrite(false);
        maskTarget.clear(Minecraft.ON_OSX);
        maskTarget.bindWrite(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        //Render loop
        PoseStack ms = new PoseStack();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        // Start of draw logic
        int[] first = mc.player.getMainHandItem().getTag().getIntArray("FirstPos");
        Vector3d firstPos = new Vector3d(first[0], first[1], first[2]);
        Vector3d secondPos;
        if(mc.hitResult instanceof BlockHitResult blockHit) {
            secondPos = new Vector3d(blockHit.getBlockPos().getX(), blockHit.getBlockPos().getY(), blockHit.getBlockPos().getZ());
        }
        else {
            secondPos = firstPos;
        }

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        BoundaryRenderer.renderCuboidFaces(ms, builder, camera, firstPos, secondPos, new Vector4d(255,255,255,255), false);

        RenderSystem.disableCull();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.enableCull();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        maskTarget.unbindWrite();
        mc.getMainRenderTarget().bindWrite(false);
        multiPostChain.process();
    }

}
