package com.ostrowski.dieroller;

import com.ostrowski.graphics.model.Tuple2;

public class TextureMapper
{
   public static Tuple2 getTextureMap(float x,   float y,
                                      float vx0, float vy0,
                                      float vx1, float vy1,
                                      float vx2, float vy2,
                                      Tuple2 textureCoordinate0, Tuple2 textureCoordinate1, Tuple2 textureCoordinate2) {
      //                1
      //           *      *
      //      *         X   *
      // 0                    *
      //                        2
      //
      // find the point along the 0-1 line where the X meets it, along a line parallel to the 1-2 line.
      // find the equation for the line from 0-1
      double slopeV01 = (vy1 - vy0)/((vx1 == vx0) ? 0.0000001 : (vx1 - vx0)); // rise / run
      //     vy0 = m01 * vx0 + b01
      //     b01 = vy0 - m01 * vx0
      double b01 = vy0 - (slopeV01 * vx0);

      double slopeV12 = (vy2 - vy1)/((vx2 == vx1) ? 0.0000001 : (vx2 - vx1)); // rise / run
      double b12 = vy1 - (slopeV12 * vx1);
      // find a line parallel to 1-2 that passes through (x,y)
      float percentAlong01Line = getPercentAlongLine(x, y, vx0, vy0, vx1, vy1, slopeV01, b01, slopeV12);

      // Now find the point on the 1-2 line where a line parallel to 0-1 passes through (x,y) and intersects the 1-2 line.
      float percentAlong12Line = getPercentAlongLine(x, y, vx1, vy1, vx2, vy2, slopeV12, b12, slopeV01);

      // Now that we have the percent along line 0-1 and along line 1-2, map those into the texture map space:
      // find the point along 0-1 in texture map space:
      Tuple2 point01 = textureCoordinate0.add(textureCoordinate1.subtract(textureCoordinate0).multiply(percentAlong01Line));
      double dx = textureCoordinate1.getX() - textureCoordinate0.getX();
      double slopeT01 = (textureCoordinate1.getY() - textureCoordinate0.getY()) / ((dx == 0) ? 0.000001 : dx);

      Tuple2 point12 = textureCoordinate1.add(textureCoordinate2.subtract(textureCoordinate1).multiply(percentAlong12Line));
      dx = (textureCoordinate2.getX() - textureCoordinate1.getX());
      double slopeT12 = (textureCoordinate2.getY() - textureCoordinate1.getY()) / ((dx == 0) ? 0.000001 : dx);
      //     b = y - mx
      double bT12 = point01.getY() - (slopeT12 * point01.getX());
      double bT01 = point12.getY() - (slopeT01 * point12.getX());

      // Now find the intersection point
      //    y = slopeT01 * x + bT01
      //    y = slopeT12 * x + bT12
      //    slopeT01 * x + bT01 = slopeT12 * x + bT12
      //    slopeT01 * x - slopeT12 * x = bT12 - bT01
      //    x * (slopeT01 - slopeT12) = bT12 - bT01
      //    x = (bT12 - bT01) / (slopeT01 - slopeT12)
      double xT = (bT12 - bT01) / (slopeT01 - slopeT12);
      double yT = (slopeT01 * xT) + bT01;
      return new Tuple2((float)xT, 1+(float)yT);
   }

   private static float getPercentAlongLine(float x, float y, float vx0, float vy0, float vx1, float vy1,
                                             double slopeV01, double b01, double slopeV12) {
      //    y = mx + b
      //    b = y - mx
      double b = y - (slopeV12 * x);
      // now find where that line meets the 0-1 line:
      //    y = m1x + b1
      //    y = m2x + b2
      //    m1x + b1 = m2x + b2
      //    m1x - m2x = b2 - b1
      //    (m1 - m2)x = b2 - b1
      //    x = (b2 - b1) / (m1 - m2);
      double xIntercept = (b - b01) / ((slopeV01 == slopeV12) ? 0.000001 : (slopeV01 - slopeV12));
      double yIntercept = (xIntercept * slopeV01) + b01;
      return (float) (lineLength(vx0, vy0, xIntercept, yIntercept) / lineLength(vx0, vy0, vx1, vy1));
   }

   private static double lineLength(double x0, double y0, double x1, double y1) {
      return Math.sqrt(((x1 - x0) * (x1 - x0)) + ((y1 - y0)*(y1 - y0)));
   }
}
