package com.ostrowski.graphics.model;

public class ColoredFace extends Face
{
   private int _colorRGB;
   public ColoredFace(Face base, int colorRGB) {
      super (base._vertexCount);
      for (int i=0 ; i<base._vertexCount ; i++) {
         addPoint(base._verts[i].clone(), base._texs[i].clone(), base._norms[i].clone());
      }
      _colorRGB = colorRGB;
   }

   public int getColor() {
      return _colorRGB;
   }

   public void setColor(int newColorRGB) {
      _colorRGB = newColorRGB;
   }
}
