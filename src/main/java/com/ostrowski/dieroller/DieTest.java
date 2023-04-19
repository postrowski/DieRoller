package com.ostrowski.dieroller;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ostrowski.graphics.Model;
import com.ostrowski.graphics.World3D;
import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.Tuple2;
import com.ostrowski.graphics.model.Tuple3;

public class DieTest
{

   @Test
   public void testRotateToNormal() {
      List<Tuple3> testVectors = new ArrayList<>();
      testVectors.add(new Tuple3( 1, 1, 1));
      testVectors.add(new Tuple3( 1, 1,-1));
      testVectors.add(new Tuple3( 1,-1, 1));
      testVectors.add(new Tuple3( 1,-1,-1));
      testVectors.add(new Tuple3(-1, 1, 1));
      testVectors.add(new Tuple3(-1, 1,-1));
      testVectors.add(new Tuple3(-1,-1, 1));
      testVectors.add(new Tuple3(-1,-1,-1));
      for (Tuple3 testVector : testVectors) {
         Tuple3 rotationVect = Model.getRotationToOrientNormalToZaxis(testVector);
         Matrix3x3 rotationMatrix = Matrix3x3.getRotationalTransformation(rotationVect.getX(), rotationVect.getY(), rotationVect.getZ());
         Tuple3 testResult = rotationMatrix.multiply(testVector);
         Assert.assertTrue(Math.abs(testResult.getX()) < .00001);
         Assert.assertTrue(Math.abs(testResult.getY()) < .00001);
         Assert.assertTrue((Math.abs(testResult.getZ()) - testVector.magnitude()) < .00001);
      }
   }

   @Test
   public void test() {
      int x = 40;
      int y = 30;
      // (0,0)
      // (0,10) **********************(60, 10)
      //      *                           *
      //               *     (40,30)      *
      //                        *         *
      //                              (60, 50)
      Tuple2 v0 = new Tuple2(0,10);
      Tuple2 v1 = new Tuple2(60,10);
      Tuple2 v2 = new Tuple2(60,50);
      Tuple2 texCoord0 = new Tuple2(0, 10);
      Tuple2 texCoord1 = new Tuple2(60, 10);
      Tuple2 texCoord2 = new Tuple2(60, 50);
      Tuple2 results = TextureMapper.getTextureMap(x, y, v0.getX(), v0.getY(), v1.getX(), v1.getY(), v2.getX(), v2.getY(), texCoord0, texCoord1, texCoord2);
      Assert.assertTrue(Math.abs((results.getX() - x)/x) < 0.05);
      Assert.assertTrue(Math.abs((results.getY() - y)/y) < 0.05);

      x = 70;
      y = 30;
      // (0,0)
      //                  (60, 10)
      //             *              *
      //        *            (70,30)   *
      // (0,40)                            *
      //                                      (90, 50)
      v0 = new Tuple2(0,40);
      v1 = new Tuple2(60,10);
      v2 = new Tuple2(90,50);
      texCoord0 = new Tuple2(0, 40);
      texCoord1 = new Tuple2(60, 10);
      texCoord2 = new Tuple2(90, 50);
      results = TextureMapper.getTextureMap(x, y, v0.getX(), v0.getY(), v1.getX(), v1.getY(), v2.getX(), v2.getY(), texCoord0, texCoord1, texCoord2);
      Assert.assertTrue(Math.abs((results.getX() - x)/x) < 0.05);
      Assert.assertTrue(Math.abs((results.getY() - y)/y) < 0.05);
   }

   @Test
   public void testSpeed() {
      //long startTime = System.currentTimeMillis();
      for (int i=0 ; i < 1_000 ; i++) {
         for (int x=-10 ; x< 110 ; x++) {
            for (int y=-10 ; y< 110 ; y++) {
               World3D.isInsideByArea(0, 0, 100, 0, 0, 100, x, y);
            }
         }
      }
//      long midTime = System.currentTimeMillis();
      for (int i=0 ; i < 1_000 ; i++) {
         for (int x=-10 ; x< 110 ; x++) {
            for (int y=-10 ; y< 110 ; y++) {
               World3D.isInside(0, 0, 100, 0, 0, 100, x, y);
            }
         }
      }
//      long endTime = System.currentTimeMillis();
//      long durationArea = midTime - startTime;
//      long durationDotProd = endTime - midTime;
      for (int x=-10 ; x< 110 ; x++) {
         for (int y=-10 ; y< 110 ; y++) {
            // (0,0)------------(100,0)
            //    |           /
            //    |     /
            // (0,100)
            boolean areaMethod       = World3D.isInsideByArea(0, 0, 100, 0, 0, 100, x, y);
            boolean dotProductMethod = World3D.isInside(0, 0, 100, 0, 0, 100, x, y);
            if (areaMethod != dotProductMethod) {
               World3D.isInsideByArea(0, 0, 100, 0, 0, 100, x, y);
               World3D.isInside(0, 0, 100, 0, 0, 100, x, y);
               Assert.fail();
            }
         }
      }
   }
}
