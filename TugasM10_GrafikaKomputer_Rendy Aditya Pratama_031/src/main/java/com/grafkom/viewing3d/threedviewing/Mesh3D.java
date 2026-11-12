/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grafkom.viewing3d.threedviewing;

/**
 *
 * @author Rendy Aditya
 */

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh3D {
    private int vaoID, vboID;
    private int vertexCount;

    public Mesh3D(float[] vertices) {
        this.vertexCount = vertices.length / 8; // 3 pos + 3 normal + 2 uv
        vaoID = glGenVertexArrays();
        vboID = glGenBuffers();

        glBindVertexArray(vaoID);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 8 * Float.BYTES;
        // layout (location = 0) aPos
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        // layout (location = 1) aNormal
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        // layout (location = 2) aTexCoords
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    // Generator Model Sphere 3D dengan pilihan Smooth Normal vs Flat Face Normal (Indikator 1 & 2)
    public static Mesh3D createSphere(float radius, int sectors, int stacks, boolean smoothNormals) {
        List<Float> data = new ArrayList<>();
        for (int i = 0; i < stacks; ++i) {
            float stackAngle1 = (float) (Math.PI / 2 - i * (Math.PI / stacks));
            float stackAngle2 = (float) (Math.PI / 2 - (i + 1) * (Math.PI / stacks));

            for (int j = 0; j < sectors; ++j) {
                float sectorAngle1 = (float) (j * (2 * Math.PI / sectors));
                float sectorAngle2 = (float) ((j + 1) * (2 * Math.PI / sectors));

                Vector3f v1 = getSpherePoint(radius, stackAngle1, sectorAngle1);
                Vector3f v2 = getSpherePoint(radius, stackAngle2, sectorAngle1);
                Vector3f v3 = getSpherePoint(radius, stackAngle1, sectorAngle2);
                Vector3f v4 = getSpherePoint(radius, stackAngle2, sectorAngle2);

                float u1 = (float) j / sectors, v_1 = (float) i / stacks;
                float u2 = (float) j / sectors, v_2 = (float) (i + 1) / stacks;
                float u3 = (float) (j + 1) / sectors, v_3 = (float) i / stacks;
                float u4 = (float) (j + 1) / sectors, v_4 = (float) (i + 1) / stacks;

                addTriangle(data, v1, v2, v3, u1, v_1, u2, v_2, u3, v_3, smoothNormals);
                addTriangle(data, v3, v2, v4, u3, v_3, u2, v_2, u4, v_4, smoothNormals);
            }
        }
        float[] arr = new float[data.size()];
        for (int i = 0; i < data.size(); i++) arr[i] = data.get(i);
        return new Mesh3D(arr);
    }

    private static Vector3f getSpherePoint(float r, float stackAngle, float sectorAngle) {
        float xy = r * (float) Math.cos(stackAngle);
        float z = r * (float) Math.sin(stackAngle);
        float x = xy * (float) Math.cos(sectorAngle);
        float y = xy * (float) Math.sin(sectorAngle);
        return new Vector3f(x, z, y);
    }

    private static void addTriangle(List<Float> list, Vector3f p1, Vector3f p2, Vector3f p3,
                                    float u1, float v1, float u2, float v2, float u3, float v3,
                                    boolean smooth) {
        Vector3f n1, n2, n3;
        if (smooth) {
            n1 = new Vector3f(p1).normalize();
            n2 = new Vector3f(p2).normalize();
            n3 = new Vector3f(p3).normalize();
        } else {
            // Flat Shading: Normal seragam untuk satu bidang face segitiga
            Vector3f edge1 = new Vector3f(p2).sub(p1);
            Vector3f edge2 = new Vector3f(p3).sub(p1);
            Vector3f faceNormal = edge1.cross(edge2).normalize();
            n1 = n2 = n3 = faceNormal;
        }
        putVertex(list, p1, n1, u1, v1);
        putVertex(list, p2, n2, u2, v2);
        putVertex(list, p3, n3, u3, v3);
    }

    private static void putVertex(List<Float> list, Vector3f p, Vector3f n, float u, float v) {
        list.add(p.x); list.add(p.y); list.add(p.z);
        list.add(n.x); list.add(n.y); list.add(n.z);
        list.add(u);   list.add(v);
    }

    // Generator Bidang Lantai (Ground Plane) untuk Menampung Bayangan (Indikator 5)
    public static Mesh3D createGroundPlane(float size) {
        float[] planeVertices = {
            // Positions          // Normals         // TexCoords
            -size, -1.5f, -size,  0.0f, 1.0f, 0.0f,  0.0f, size,
            -size, -1.5f,  size,  0.0f, 1.0f, 0.0f,  0.0f, 0.0f,
             size, -1.5f,  size,  0.0f, 1.0f, 0.0f,  size, 0.0f,

            -size, -1.5f, -size,  0.0f, 1.0f, 0.0f,  0.0f, size,
             size, -1.5f,  size,  0.0f, 1.0f, 0.0f,  size, 0.0f,
             size, -1.5f, -size,  0.0f, 1.0f, 0.0f,  size, size
        };
        return new Mesh3D(planeVertices);
    }
}