package com.ostrowski.graphics.model;

public class ColoredFace extends Face
{
   private int colorRGB;
   public ColoredFace(Face base, int colorRGB) {
      super(base.vertexCount);
      for (int i = 0; i<base.vertexCount; i++) {
         addPoint(base.verts[i].clone(), base.texs[i].clone(), base.norms[i].clone());
      }
      this.colorRGB = colorRGB;
   }

   public int getColor() {
      return colorRGB;
   }

   public void setColor(int newColorRGB) {
      colorRGB = newColorRGB;
   }
}
