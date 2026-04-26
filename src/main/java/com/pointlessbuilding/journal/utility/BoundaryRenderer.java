package com.pointlessbuilding.journal.utility;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BoundaryRenderer {
    
    public static Direction getDirection(Vector3d origin, Vector3d destination) {
        if(origin.x() < destination.x()) return Direction.EAST;
        else if(origin.x() > destination.x()) return Direction.WEST;
        else if(origin.y() < destination.y()) return Direction.UP;
        else if(origin.y() > destination.y()) return Direction.DOWN;
        else if(origin.z() < destination.z()) return Direction.SOUTH;
        else if(origin.z() > destination.z()) return Direction.NORTH;
        else return Direction.NORTH;
    }

    // Essentially, starting from origin and ending at destination offset by width/2, render a cuboid.
    public static void renderThickLine(PoseStack.Pose pose, VertexConsumer consumer, Vector3d origin, Vector3d destination, float width, Vector4d color) {

        float r = (float)color.x()/255f; float g = (float)color.y()/255f; float b = (float)color.z()/255f; float a = (float)color.w()/255f;

        switch (getDirection(origin, destination)) {
            case NORTH, SOUTH -> {
                origin.add(-width/2, -width/2, 0);
                destination.add(width/2, width/2, 0);
            }
            case EAST, WEST -> {
                origin.add(0, -width/2, -width/2);
                destination.add(0, width/2, width/2);
            }
            case UP, DOWN -> {
                origin.add(-width/2, 0, -width/2);
                destination.add(width/2, 0, width/2);
            }
        }

        double ox = origin.x(); double oy = origin.y(); double oz = origin.z();
        double dx = destination.x(); double dy = destination.y(); double dz = destination.z();

        //Vertex order is -X,-Y,-Z counter clockwise and then incremented by Y
        //Vertex 0
        Vector4d v0 = new Vector4d(ox, oy, oz, 1.0);
        v0.mul(pose.pose());

        //Vertex 1
        Vector4d v1 = new Vector4d(dx, oy, oz, 1.0);
        v1.mul(pose.pose());

        //Vertex 2
        Vector4d v2 = new Vector4d(dx, oy, dz, 1.0);
        v2.mul(pose.pose());

        //Vertex 3
        Vector4d v3 = new Vector4d(ox, oy, dz, 1.0);
        v3.mul(pose.pose());

        //Vertex 4
        Vector4d v4 = new Vector4d(ox, dy, oz, 1.0);
        v4.mul(pose.pose());

        //Vertex 5
        Vector4d v5 = new Vector4d(dx, dy, oz, 1.0);
        v5.mul(pose.pose());

        //Vertex 6
        Vector4d v6 = new Vector4d(dx, dy, dz, 1.0);
        v6.mul(pose.pose());

        //Vertex 7
        Vector4d v7 = new Vector4d(ox, dy, dz, 1.0);
        v7.mul(pose.pose());

        Vector3f nx = new Vector3f(1,0,0);
        nx.mul(pose.normal());
        Vector3f ny = new Vector3f(0,1,0);
        ny.mul(pose.normal());
        Vector3f nz = new Vector3f(0,0,1);
        nz.mul(pose.normal());

        //v0->v1
        consumer.vertex(v0.x(), v0.y(), v0.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();
        consumer.vertex(v1.x(), v1.y(), v1.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();

        //v1->v2
        consumer.vertex(v1.x(), v1.y(), v1.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();
        consumer.vertex(v2.x(), v2.y(), v2.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();

        //v2->v3
        consumer.vertex(v2.x(), v2.y(), v2.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();
        consumer.vertex(v3.x(), v3.y(), v3.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();

        //v3->v0
        consumer.vertex(v3.x(), v3.y(), v3.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();
        consumer.vertex(v0.x(), v0.y(), v0.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();

        //v4->v5
        consumer.vertex(v4.x(), v4.y(), v4.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();
        consumer.vertex(v5.x(), v5.y(), v5.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();

        //v5->v6
        consumer.vertex(v5.x(), v5.y(), v5.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();
        consumer.vertex(v6.x(), v6.y(), v6.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();

        //v6->v7
        consumer.vertex(v6.x(), v6.y(), v6.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();
        consumer.vertex(v7.x(), v7.y(), v7.z()).color(r,g,b,a).normal(nx.x(),nx.y(),nx.z()).endVertex();

        //v7->v4
        consumer.vertex(v7.x(), v7.y(), v7.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();
        consumer.vertex(v4.x(), v4.y(), v4.z()).color(r,g,b,a).normal(nz.x(),nz.y(),nz.z()).endVertex();

        //v0->v4
        consumer.vertex(v0.x(), v0.y(), v0.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();
        consumer.vertex(v4.x(), v4.y(), v4.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();

        //v1->v5
        consumer.vertex(v1.x(), v1.y(), v1.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();
        consumer.vertex(v5.x(), v5.y(), v5.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();

        //v2->v6
        consumer.vertex(v2.x(), v2.y(), v2.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();
        consumer.vertex(v6.x(), v6.y(), v6.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();

        //v3->v7
        consumer.vertex(v3.x(), v3.y(), v3.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();
        consumer.vertex(v7.x(), v7.y(), v7.z()).color(r,g,b,a).normal(ny.x(),ny.y(),ny.z()).endVertex();

    }

    public static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, Vector3d v0, Vector3d v1, Vector3d v2, Vector3d v3, Vector3f normal, Vector4d color) {
        
        float r = (float)color.x()/255f; float g = (float)color.y()/255f; float b = (float)color.z()/255f; float a = (float)color.w()/255f;
        int no_overlay = OverlayTexture.NO_OVERLAY;
        int lightmap = LightTexture.FULL_BRIGHT;

        Vector4d vt0 = new Vector4d(v0,1);
        vt0.mul(pose.pose());
        Vector4d vt1 = new Vector4d(v1,1);
        vt1.mul(pose.pose());
        Vector4d vt2 = new Vector4d(v2,1);
        vt2.mul(pose.pose());
        Vector4d vt3 = new Vector4d(v3,1);
        vt3.mul(pose.pose());

        normal.mul(pose.normal());

        consumer.vertex(vt0.x(), vt0.y(), vt0.z()).color(r,g,b,a).uv(0,0).overlayCoords(no_overlay).uv2(lightmap).normal(normal.x(), normal.y(), normal.z()).endVertex();
        consumer.vertex(vt1.x(), vt1.y(), vt1.z()).color(r,g,b,a).uv(1,0).overlayCoords(no_overlay).uv2(lightmap).normal(normal.x(), normal.y(), normal.z()).endVertex();
        consumer.vertex(vt2.x(), vt2.y(), vt2.z()).color(r,g,b,a).uv(1,1).overlayCoords(no_overlay).uv2(lightmap).normal(normal.x(), normal.y(), normal.z()).endVertex();
        consumer.vertex(vt3.x(), vt3.y(), vt3.z()).color(r,g,b,a).uv(0,1).overlayCoords(no_overlay).uv2(lightmap).normal(normal.x(), normal.y(), normal.z()).endVertex();

    }

    public static void renderCuboid(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vector3d origin, Vector3d destination, Vector4d color) {
        
        float width = 0.02f;

        if(destination.x >= origin.x) destination.add(1,0,0);
        else origin.add(1,0,0);
        if(destination.y >= origin.y) destination.add(0,1,0);
        else origin.add(0,1,0);
        if(destination.z >= origin.z) destination.add(0,0,1);
        else origin.add(0,0,1);

        Vector3d v0 = origin;
        Vector3d v1 = new Vector3d(destination.x(), origin.y(), origin.z());
        Vector3d v2 = new Vector3d(destination.x(), origin.y(), destination.z());
        Vector3d v3 = new Vector3d(origin.x(), origin.y(), destination.z());
        Vector3d v4 = new Vector3d(origin.x(), destination.y(), origin.z());
        Vector3d v5 = new Vector3d(destination.x(), destination.y(), origin.z());
        Vector3d v6 = destination;
        Vector3d v7 = new Vector3d(origin.x(), destination.y(), destination.z());
        

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        // Time to render each line!
        renderThickLine(poseStack.last(), consumer, v0, v1, width, color);
        renderThickLine(poseStack.last(), consumer, v1, v2, width, color);
        renderThickLine(poseStack.last(), consumer, v2, v3, width, color);
        renderThickLine(poseStack.last(), consumer, v3, v0, width, color);
        renderThickLine(poseStack.last(), consumer, v4, v5, width, color);
        renderThickLine(poseStack.last(), consumer, v5, v6, width, color);
        renderThickLine(poseStack.last(), consumer, v6, v7, width, color);
        renderThickLine(poseStack.last(), consumer, v7, v4, width, color);
        renderThickLine(poseStack.last(), consumer, v0, v4, width, color);
        renderThickLine(poseStack.last(), consumer, v1, v5, width, color);
        renderThickLine(poseStack.last(), consumer, v2, v6, width, color);
        renderThickLine(poseStack.last(), consumer, v3, v7, width, color);

        poseStack.popPose();

    }

    public static void renderCuboidFaces(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vector3d origin, Vector3d destination, Vector4d color) {

        Vector3d v0 = origin;
        Vector3d v1 = new Vector3d(destination.x(), origin.y(), origin.z());
        Vector3d v2 = new Vector3d(destination.x(), origin.y(), destination.z());
        Vector3d v3 = new Vector3d(origin.x(), origin.y(), destination.z());
        Vector3d v4 = new Vector3d(origin.x(), destination.y(), origin.z());
        Vector3d v5 = new Vector3d(destination.x(), destination.y(), origin.z());
        Vector3d v6 = destination;
        Vector3d v7 = new Vector3d(origin.x(), destination.y(), destination.z());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        renderFace(poseStack.last(), consumer, v0, v1, v2, v3, new Vector3f(0,-1,0), color);
        renderFace(poseStack.last(), consumer, v4, v5, v6, v7, new Vector3f(0,1,0), color);
        renderFace(poseStack.last(), consumer, v4, v5, v1, v0, new Vector3f(0,0,-1), color);
        renderFace(poseStack.last(), consumer, v7, v6, v2, v3, new Vector3f(0,0,1), color);
        renderFace(poseStack.last(), consumer, v4, v7, v3, v0, new Vector3f(-1,0,0), color);
        renderFace(poseStack.last(), consumer, v5, v6, v2, v1, new Vector3f(1,0,0), color);

        poseStack.popPose();

    }

}
