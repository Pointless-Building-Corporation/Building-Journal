package com.pointlessbuilding.journal.client;

import org.joml.Vector3d;
import org.joml.Vector4d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pointlessbuilding.journal.BuildingJournal;
import com.pointlessbuilding.journal.items.BuildersCompass;
import com.pointlessbuilding.journal.utility.BoundaryRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingJournal.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientRenderEvents {
    
    private static ResourceLocation dummyLocation = new ResourceLocation(BuildingJournal.MODID,"textures/misc/dummy.png");

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)
            renderCompassBoundaries(event);
        else
            return;
    }

    protected static void renderCompassBoundaries(RenderLevelStageEvent event) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        ItemStack held = player.getMainHandItem();
        if(!(held.getItem() instanceof BuildersCompass)) return;

        if(!held.hasTag() || !held.getTag().contains("FirstPos")) return;

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
        BoundaryRenderer.renderCuboidFaces(ms, faceConsumer, camera, firstPos, secondPos, new Vector4d(93,215,251,128));
        bufferSource.endBatch(RenderType.entityTranslucent(dummyLocation));
    }

}
