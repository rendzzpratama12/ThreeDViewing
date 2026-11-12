/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.grafkom.viewing3d.threedviewing;

/**
 *
 * @author Rendy Aditya
 */

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera3D {
    private Vector3f eye;
    private Vector3f target;
    private Vector3f up;

    public Camera3D(Vector3f eye, Vector3f target, Vector3f up) {
        this.eye = new Vector3f(eye);
        this.target = new Vector3f(target);
        this.up = new Vector3f(up);
    }

    public Matrix4f calculateLookAtMatrix() {
        Vector3f n = new Vector3f();
        eye.sub(target, n).normalize();

        Vector3f u = new Vector3f();
        up.cross(n, u).normalize();

        Vector3f v = new Vector3f();
        n.cross(u, v);

        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.m00(u.x).m10(u.y).m20(u.z).m30(-u.dot(eye));
        viewMatrix.m01(v.x).m11(v.y).m21(v.z).m31(-v.dot(eye));
        viewMatrix.m02(n.x).m12(n.y).m22(n.z).m32(-n.dot(eye));
        viewMatrix.m03(0.0f).m13(0.0f).m23(0.0f).m33(1.0f);

        return viewMatrix;
    }

    public void updateOrbitalPosition(float radius, float azimuthDeg, float elevationDeg) {
        float azRad = (float) Math.toRadians(azimuthDeg);
        float elRad = (float) Math.toRadians(elevationDeg);

        this.eye.x = radius * (float)(Math.cos(elRad) * Math.sin(azRad));
        this.eye.y = radius * (float)(Math.sin(elRad));
        this.eye.z = radius * (float)(Math.cos(elRad) * Math.cos(azRad));
    }

    public Vector3f getEye() { return eye; }
    public Vector3f getTarget() { return target; }
    public Vector3f getUp() { return up; }
}

