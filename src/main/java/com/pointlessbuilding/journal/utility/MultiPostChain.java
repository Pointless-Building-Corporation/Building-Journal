package com.pointlessbuilding.journal.utility;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPostChain implements AutoCloseable{

    private final RenderTarget screenTarget;
    private final ResourceManager resourceManager;
    private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
    private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
    private EffectInstance effect;
    private EffectInstance blitEffect;
    public Matrix4f shaderOrthoMatrix;
    public int screenWidth;
    public int screenHeight;

    public MultiPostChain(ResourceManager resourceManager, RenderTarget screenTarget) throws IOException{
        this.resourceManager = resourceManager;
        this.screenTarget = screenTarget;
        this.screenWidth = screenTarget.viewWidth;
        this.screenHeight = screenTarget.viewHeight;
        this.updateOrthoMatrix();

        this.addTempTarget("swap", this.screenWidth, this.screenHeight);
        this.addTempTarget("mask", this.screenWidth, this.screenHeight);
        this.blitEffect = new EffectInstance(this.resourceManager, "blit");
        this.addPass("buildingjournal:blueprint_shader");
    }

    public RenderTarget getTempTarget(String attributeName) {
        return this.customRenderTargets.get(attributeName);
    }

    public void addTempTarget(String name, int width, int height) {
        RenderTarget rendertarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        if (screenTarget.isStencilEnabled()) { rendertarget.enableStencil(); }
        this.customRenderTargets.put(name, rendertarget);
        if (width == this.screenWidth && height == this.screenHeight) {
            this.fullSizedTargets.add(rendertarget);
        }
    }

    private void updateOrthoMatrix() {
    this.shaderOrthoMatrix = (new Matrix4f()).setOrtho(
        0.0F, (float)this.screenTarget.viewWidth,
        0.0F, (float)this.screenTarget.viewHeight,
        0.1F, 1000.0F);
}

    public void addPass(String programName) throws IOException {
        this.effect = new EffectInstance(this.resourceManager, programName);
    }

    public void resize(int width, int height) {
        this.screenWidth = this.screenTarget.width;
        this.screenHeight = this.screenTarget.height;
        this.updateOrthoMatrix();

        if(this.effect != null) {
            this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        }

        for(RenderTarget rendertarget : this.fullSizedTargets) {
            rendertarget.resize(width, height, Minecraft.ON_OSX);
        }
    }

    public void process() {
        RenderTarget maskTarget = this.customRenderTargets.get("mask");
        RenderTarget swapTarget = this.customRenderTargets.get("swap");

        //Pass 1: screenTarget to swapTarget
        this.screenTarget.unbindWrite();
        float w = (float)swapTarget.width;
        float h = (float)swapTarget.height;
        RenderSystem.viewport(0, 0, (int)w, (int)h);

        this.effect.setSampler("DiffuseSampler", this.screenTarget::getColorTextureId);
        this.effect.setSampler("MaskSampler", maskTarget::getColorTextureId);
        this.effect.safeGetUniform("AuxSize0").set((float)maskTarget.width, (float)maskTarget.height);
        this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        this.effect.safeGetUniform("InSize").set((float)this.screenTarget.viewWidth, (float)this.screenTarget.viewHeight);
        this.effect.safeGetUniform("OutSize").set(w, h);
        this.effect.apply();

        swapTarget.clear(Minecraft.ON_OSX);
        swapTarget.bindWrite(false);
        RenderSystem.depthFunc(519);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex((double)w, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex((double)w, (double)h, 500.0D).endVertex();
        bufferbuilder.vertex(0.0D, (double)h, 500.0D).endVertex();
        BufferUploader.draw(bufferbuilder.end());
        RenderSystem.depthFunc(515);
        this.effect.clear();
        swapTarget.unbindWrite();
        this.screenTarget.unbindRead();
        maskTarget.unbindRead();

        //Pass 2: swapTarget to screenTarget
        swapTarget.unbindWrite();
        float w2 = (float)this.screenTarget.viewWidth;
        float h2 = (float)this.screenTarget.viewHeight;
        RenderSystem.viewport(0,0,(int)w2, (int)h2);

        this.blitEffect.setSampler("DiffuseSampler", swapTarget::getColorTextureId);
        this.blitEffect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        this.blitEffect.safeGetUniform("InSize").set((float)swapTarget.width, (float)swapTarget.height);
        this.blitEffect.safeGetUniform("OutSize").set(w2,h2);
        this.blitEffect.apply();

        this.screenTarget.bindWrite(false);
        RenderSystem.depthFunc(519);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex((double)w2, 0.0D, 500.0D).endVertex();
        bufferbuilder.vertex((double)w2, (double)h2, 500.0D).endVertex();
        bufferbuilder.vertex(0.0D, (double)h2, 500.0D).endVertex();
        BufferUploader.draw(bufferbuilder.end());
        RenderSystem.depthFunc(515);
        this.blitEffect.clear();
        //this.screenTarget.unbindWrite();
        maskTarget.unbindRead();
    }

    @Override
    public void close() {
        for(RenderTarget rendertarget : this.customRenderTargets.values()) {
            rendertarget.destroyBuffers();
        }

        if (this.effect != null) {
            this.effect.close();
        }
   }
    
}
