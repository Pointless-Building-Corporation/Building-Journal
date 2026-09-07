package com.pointlessbuilding.journal.utility;

import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BoundaryRenderer {
    
    public static Direction getDirection(Vector3f origin, Vector3f destination) {
        if(origin.x() < destination.x()) return Direction.EAST;
        else if(origin.x() > destination.x()) return Direction.WEST;
        else if(origin.y() < destination.y()) return Direction.UP;
        else if(origin.y() > destination.y()) return Direction.DOWN;
        else if(origin.z() < destination.z()) return Direction.SOUTH;
        else if(origin.z() > destination.z()) return Direction.NORTH;
        else return Direction.NORTH;
    }

    // Essentially, starting from origin and ending at destination offset by width/2, render a cuboid.
    public static void renderThickLine(PoseStack.Pose pose, VertexConsumer consumer, Vector3f origin, Vector3f destination, float width, Vector4d color) {

        float r = (float)color.x()/255f; float g = (float)color.y()/255f; float b = (float)color.z()/255f; float a = (float)color.w()/255f;

        switch (getDirection(origin, destination)) {
            case NORTH, SOUTH -> {
                origin.add(-width/2, -width/2, -width/2);
                destination.add(width/2, width/2, width/2);
            }
            case EAST, WEST -> {
                origin.add(-width/2, -width/2, -width/2);
                destination.add(width/2, width/2, width/2);
            }
            case UP, DOWN -> {
                origin.add(-width/2, -width/2, -width/2);
                destination.add(width/2, width/2, width/2);
            }
        }

        float ox = origin.x(); float oy = origin.y(); float oz = origin.z();
        float dx = destination.x(); float dy = destination.y(); float dz = destination.z();

        //Vertex order is -X,-Y,-Z counter clockwise and then incremented by Y
        //Vertex 0
        Vector4f v0 = new Vector4f(ox, oy, oz, 1.0f);
        v0.mul(pose.pose());

        //Vertex 1
        Vector4f v1 = new Vector4f(dx, oy, oz, 1.0f);
        v1.mul(pose.pose());

        //Vertex 2
        Vector4f v2 = new Vector4f(dx, oy, dz, 1.0f);
        v2.mul(pose.pose());

        //Vertex 3
        Vector4f v3 = new Vector4f(ox, oy, dz, 1.0f);
        v3.mul(pose.pose());

        //Vertex 4
        Vector4f v4 = new Vector4f(ox, dy, oz, 1.0f);
        v4.mul(pose.pose());

        //Vertex 5
        Vector4f v5 = new Vector4f(dx, dy, oz, 1.0f);
        v5.mul(pose.pose());

        //Vertex 6
        Vector4f v6 = new Vector4f(dx, dy, dz, 1.0f);
        v6.mul(pose.pose());

        //Vertex 7
        Vector4f v7 = new Vector4f(ox, dy, dz, 1.0f);
        v7.mul(pose.pose());

        Vector3f nx = new Vector3f(1,0,0);
        nx.mul(pose.normal());
        Vector3f ny = new Vector3f(0,1,0);
        ny.mul(pose.normal());
        Vector3f nz = new Vector3f(0,0,1);
        nz.mul(pose.normal());

        //v0->v1
        consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());
        consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());

        //v1->v2
        consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());
        consumer.addVertex(v2.x(), v2.y(), v2.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());

        //v2->v3
        consumer.addVertex(v2.x(), v2.y(), v2.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());
        consumer.addVertex(v3.x(), v3.y(), v3.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());

        //v3->v0
        consumer.addVertex(v3.x(), v3.y(), v3.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());
        consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());

        //v4->v5
        consumer.addVertex(v4.x(), v4.y(), v4.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());
        consumer.addVertex(v5.x(), v5.y(), v5.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());

        //v5->v6
        consumer.addVertex(v5.x(), v5.y(), v5.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());
        consumer.addVertex(v6.x(), v6.y(), v6.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());

        //v6->v7
        consumer.addVertex(v6.x(), v6.y(), v6.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());
        consumer.addVertex(v7.x(), v7.y(), v7.z()).setColor(r,g,b,a).setNormal(nx.x(),nx.y(),nx.z());

        //v7->v4
        consumer.addVertex(v7.x(), v7.y(), v7.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());
        consumer.addVertex(v4.x(), v4.y(), v4.z()).setColor(r,g,b,a).setNormal(nz.x(),nz.y(),nz.z());

        //v0->v4
        consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());
        consumer.addVertex(v4.x(), v4.y(), v4.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());

        //v1->v5
        consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());
        consumer.addVertex(v5.x(), v5.y(), v5.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());

        //v2->v6
        consumer.addVertex(v2.x(), v2.y(), v2.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());
        consumer.addVertex(v6.x(), v6.y(), v6.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());

        //v3->v7
        consumer.addVertex(v3.x(), v3.y(), v3.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());
        consumer.addVertex(v7.x(), v7.y(), v7.z()).setColor(r,g,b,a).setNormal(ny.x(),ny.y(),ny.z());

    }

    public static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal, Vector4d color) {
        
        float r = (float)color.x()/255f; float g = (float)color.y()/255f; float b = (float)color.z()/255f; float a = (float)color.w()/255f;
        int no_overlay = OverlayTexture.NO_OVERLAY;
        int lightmap = LightTexture.FULL_BRIGHT;

        Vector4f vt0 = new Vector4f(v0,1.0f);
        vt0.mul(pose.pose());
        Vector4f vt1 = new Vector4f(v1,1.0f);
        vt1.mul(pose.pose());
        Vector4f vt2 = new Vector4f(v2,1.0f);
        vt2.mul(pose.pose());
        Vector4f vt3 = new Vector4f(v3,1.0f);
        vt3.mul(pose.pose());

        normal.mul(pose.normal());

        consumer.addVertex(vt0.x(), vt0.y(), vt0.z()).setColor(r,g,b,a).setUv(0,0).setOverlay(no_overlay).setLight(lightmap).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(vt1.x(), vt1.y(), vt1.z()).setColor(r,g,b,a).setUv(1,0).setOverlay(no_overlay).setLight(lightmap).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(vt2.x(), vt2.y(), vt2.z()).setColor(r,g,b,a).setUv(1,1).setOverlay(no_overlay).setLight(lightmap).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(vt3.x(), vt3.y(), vt3.z()).setColor(r,g,b,a).setUv(0,1).setOverlay(no_overlay).setLight(lightmap).setNormal(normal.x(), normal.y(), normal.z());

    }

    public static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f v3, Vector4d color) {
        
        float r = (float)color.x()/255f; float g = (float)color.y()/255f; float b = (float)color.z()/255f; float a = (float)color.w()/255f;

        Vector4f vt0 = new Vector4f(v0,1.0f);
        vt0.mul(pose.pose());
        Vector4f vt1 = new Vector4f(v1,1.0f);
        vt1.mul(pose.pose());
        Vector4f vt2 = new Vector4f(v2,1.0f);
        vt2.mul(pose.pose());
        Vector4f vt3 = new Vector4f(v3,1.0f);
        vt3.mul(pose.pose());

        consumer.addVertex(vt0.x(), vt0.y(), vt0.z()).setColor(r,g,b,a);
        consumer.addVertex(vt1.x(), vt1.y(), vt1.z()).setColor(r,g,b,a);
        consumer.addVertex(vt2.x(), vt2.y(), vt2.z()).setColor(r,g,b,a);
        consumer.addVertex(vt3.x(), vt3.y(), vt3.z()).setColor(r,g,b,a);

    }

    public static void renderCuboid(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vector3f originIn, Vector3f destinationIn, Vector4d color) {
        
        float width = 0.01f;
        Vector3f origin = new Vector3f(originIn);
        Vector3f destination = new Vector3f(destinationIn);

        if(destination.x >= origin.x) destination.add(1,0,0);
        else origin.add(1,0,0);
        if(destination.y >= origin.y) destination.add(0,1,0);
        else origin.add(0,1,0);
        if(destination.z >= origin.z) destination.add(0,0,1);
        else origin.add(0,0,1);

        Vector3f v0 = origin;
        Vector3f v1 = new Vector3f(destination.x(), origin.y(), origin.z());
        Vector3f v2 = new Vector3f(destination.x(), origin.y(), destination.z());
        Vector3f v3 = new Vector3f(origin.x(), origin.y(), destination.z());
        Vector3f v4 = new Vector3f(origin.x(), destination.y(), origin.z());
        Vector3f v5 = new Vector3f(destination.x(), destination.y(), origin.z());
        Vector3f v6 = destination;
        Vector3f v7 = new Vector3f(origin.x(), destination.y(), destination.z());
        

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

    public static void renderCuboidFaces(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, Vector3f originIn, Vector3f destinationIn, Vector4d color, boolean is_translucent) {

        Vector3f origin = new Vector3f(originIn);
        Vector3f destination = new Vector3f(destinationIn);

        if(destination.x >= origin.x) destination.add(1,0,0);
        else origin.add(1,0,0);
        if(destination.y >= origin.y) destination.add(0,1,0);
        else origin.add(0,1,0);
        if(destination.z >= origin.z) destination.add(0,0,1);
        else origin.add(0,0,1);

        Vector3f v0 = origin;
        Vector3f v1 = new Vector3f(destination.x(), origin.y(), origin.z());
        Vector3f v2 = new Vector3f(destination.x(), origin.y(), destination.z());
        Vector3f v3 = new Vector3f(origin.x(), origin.y(), destination.z());
        Vector3f v4 = new Vector3f(origin.x(), destination.y(), origin.z());
        Vector3f v5 = new Vector3f(destination.x(), destination.y(), origin.z());
        Vector3f v6 = destination;
        Vector3f v7 = new Vector3f(origin.x(), destination.y(), destination.z());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        if(is_translucent) {
            renderFace(poseStack.last(), consumer, v0, v1, v2, v3, new Vector3f(0,-1,0), color);
            renderFace(poseStack.last(), consumer, v4, v5, v6, v7, new Vector3f(0,1,0), color);
            renderFace(poseStack.last(), consumer, v4, v5, v1, v0, new Vector3f(0,0,-1), color);
            renderFace(poseStack.last(), consumer, v7, v6, v2, v3, new Vector3f(0,0,1), color);
            renderFace(poseStack.last(), consumer, v4, v7, v3, v0, new Vector3f(-1,0,0), color);
            renderFace(poseStack.last(), consumer, v5, v6, v2, v1, new Vector3f(1,0,0), color);
        }
        else {
            renderFace(poseStack.last(), consumer, v0, v1, v2, v3, color);
            renderFace(poseStack.last(), consumer, v4, v5, v6, v7, color);
            renderFace(poseStack.last(), consumer, v4, v5, v1, v0, color);
            renderFace(poseStack.last(), consumer, v7, v6, v2, v3, color);
            renderFace(poseStack.last(), consumer, v4, v7, v3, v0, color);
            renderFace(poseStack.last(), consumer, v5, v6, v2, v1, color);
        }

        poseStack.popPose();

    }

}
