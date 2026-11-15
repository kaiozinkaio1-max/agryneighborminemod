package angryneighbor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import java.io.*;
import java.util.*;

/** Desenha a malha do modelo "Angry Neighbor" (by ValePro10, CC-BY-4.0) convertida para mesh/neighbor.bin. */
public class NeighborRenderer extends EntityRenderer<NeighborEntity> {
    static class Part { Identifier tex; float[] pos, uv, nor; int[] idx; }
    /** se o vizinho andar de costas, troque para 180f */
    static final float FRONT_OFFSET = 0f;
    final List<Part> parts = new ArrayList<>();

    public NeighborRenderer(EntityRendererFactory.Context ctx) {
        super(ctx); shadowRadius = 0.5f;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(MinecraftClient.getInstance().getResourceManager()
                .getResource(new Identifier(AngryNeighbor.ID, "mesh/neighbor.bin")).get().getInputStream()))) {
            int n = in.readInt();
            for (int i = 0; i < n; i++) {
                Part p = new Part(); int tex = in.readInt(), vc = in.readInt(), ic = in.readInt();
                p.tex = new Identifier(AngryNeighbor.ID, "textures/entity/n" + tex + ".png");
                p.pos = f(in, vc * 3); p.uv = f(in, vc * 2); p.nor = f(in, vc * 3);
                p.idx = new int[ic]; for (int k = 0; k < ic; k++) p.idx[k] = in.readInt();
                parts.add(p);
            }
        } catch (Exception e) { throw new RuntimeException("Falha ao ler a malha do vizinho", e); }
    }

    static float[] f(DataInputStream in, int n) throws IOException { float[] a = new float[n]; for (int i = 0; i < n; i++) a[i] = in.readFloat(); return a; }

    @Override public Identifier getTexture(NeighborEntity e) { return parts.get(0).tex; }

    @Override public void render(NeighborEntity e, float yaw, float td, MatrixStack m, VertexConsumerProvider vcp, int light) {
        m.push();
        float body = MathHelper.lerpAngleDegrees(td, e.prevBodyYaw, e.bodyYaw);
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-body + FRONT_OFFSET));
        Vec3d v = e.getVelocity();
        if (v.horizontalLengthSquared() > 0.0004) {
            float t = e.age + td;
            m.translate(0, Math.abs(Math.sin(t * 0.6)) * 0.06, 0);
            m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.sin(t * 0.6) * 4f));
            m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(8f));
        }
        MatrixStack.Entry en = m.peek(); Matrix4f pm = en.getPositionMatrix(); Matrix3f nm = en.getNormalMatrix();
        for (Part p : parts) {
            VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityCutoutNoCull(p.tex));
            for (int i = 0; i + 2 < p.idx.length; i += 3) for (int k = 0; k < 4; k++) {
                int ix = p.idx[i + Math.min(k, 2)]; // triangulo como quad (ultimo vertice repetido)
                vc.vertex(pm, p.pos[ix * 3], p.pos[ix * 3 + 1], p.pos[ix * 3 + 2]).color(255, 255, 255, 255)
                  .texture(p.uv[ix * 2], p.uv[ix * 2 + 1]).overlay(OverlayTexture.DEFAULT_UV).light(light)
                  .normal(nm, p.nor[ix * 3], p.nor[ix * 3 + 1], p.nor[ix * 3 + 2]).next();
            }
        }
        m.pop();
        super.render(e, yaw, td, m, vcp, light);
    }
}
