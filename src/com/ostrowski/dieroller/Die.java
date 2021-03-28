package com.ostrowski.dieroller;


import static com.ostrowski.graphics.World3D.UP_VECTOR;

import java.util.ArrayList;
import java.util.List;

import com.ostrowski.graphics.Frame;
import com.ostrowski.graphics.Model;
import com.ostrowski.graphics.model.ColoredFace;
import com.ostrowski.graphics.model.Face;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.Tuple3;

public class Die extends Model
{
   private final int          resultRGB = 0xA03030; // default results color is redish
   private final int          results;
   private final Tuple3       targetOrientation;
   private final List<Tuple3> directions = new ArrayList<>();
   private final int          sides;

   public Die(int sides, ObjData data, float scale, int results, Integer baseRGB, List<Tuple3> directions) {
      super(data, scale, baseRGB);
      this.sides = sides;
      this.results = results;
      if (directions != null) {
         this.directions.addAll(directions);
         // override the default direction with the first direction
         if ((this.directions != null) && (!this.directions.isEmpty())) {
            frame = frame.setVelocity(this.directions.remove(0));
         }
      }


      Face face = this.data.getFace(this.results - 1);
      Tuple3 normal = face.getCommonNormal();
      if (sides == 4) {
         normal = normal.multiply(-1.0f);
      }

      targetOrientation = normal;
      if (sides == 20) {
         frame = frame.setVelocity(frame.velocity.add(new Tuple3(10, -40, 0)));
      }
      // Orient the die so that it starts out with the results face up
      frame = frame.setUp(targetOrientation);
   }

   @Override
   public void update(float elapsedTimeInSeconds, Tuple3 acceleration, float floorZValue) {
      if (!moving) {
         return;
      }
      super.update(elapsedTimeInSeconds, acceleration, floorZValue);
   }

   @Override
   protected Frame bounce(Frame frame, Tuple3 positionedCenterMassAtFloor, Tuple3 positionedCenterMass,
                          float elapsedTimeInSeconds, Tuple3 acceleration) {
      // Use the vectors in the directions list for each bounce.
      if ((directions != null) && (!directions.isEmpty())) {
         return this.frame.setVelocity(directions.remove(0));
      }
      return super.bounce(frame, positionedCenterMassAtFloor, positionedCenterMass, elapsedTimeInSeconds, acceleration);
   }

   @Override
   public List<ColoredFace> getColoredFaces() {
      List<ColoredFace> faces = super.getColoredFaces();
      // augment the results face by shading in a contrasting color
      if (!moving) {
         int facesToFind = data.getFaceCount() / sides;
         for (ColoredFace face : faces) {
            float upness = face.getCommonNormal().dotProduct(UP_VECTOR);
            if (upness > 0.98f) {
               face.setColor(resultRGB);
               if (--facesToFind == 0) {
                  break;
               }
            }
         }
      }
      return faces;
   }


//   private void paintByRaster(GC gc) {
////    computeImage();
////    paintFromImage(gc);
//// }
////
//// public void computeImage() {
//    Region outline = getOutline();
//    Rectangle bounds = outline.getBounds();
//    outline.dispose();
//
//    List<Face> orderedFaces = new LinkedList<>();
//    HashMap<Face, Rectangle> faceToRect   = new HashMap<>();
//    HashMap<Face, Float> faceToBrightness = new HashMap<>();
//    HashMap<Face, int[]> faceToPoints     = new HashMap<>();
//
//    for (Face face : data.getFaces()) {
//       // don't draw faces that face away:
//       if (isFacingAway(face)) {
//          continue;
//       }
//
//       float brightness = lightSource.dotProduct(normal);
//       brightness = (Math.max(0, brightness) * (lightestColor - darkestColor)) + darkestColor;
//
//       int[] points = getPoints(face);
//       faceToRect.put(face, getBounds(points));
//       faceToBrightness.put(face, brightness);
//       faceToPoints.put(face, points);
//       orderedFaces.add(face);
//    }
//
////    image_red   = new int[area.width][area.height];
////    image_green = new int[area.width][area.height];
////    image_blue  = new int[area.width][area.height];
//
//    Color currentColor = null;
//
//    for (int y0=Math.max(0, 0-bounds.y) ; y0<bounds.height ; y0++) {
//       int y = y0 + bounds.y;
//       for (int x0=Math.max(0, 0-bounds.x) ; x0<bounds.width ; x0++) {
//          int x = x0 + bounds.x;
//          int entryInFaceList = 0;
//          boolean pointFound = false;
//          for (Face face : orderedFaces) {
//             entryInFaceList++;
//             if (faceToRect.get(face).contains(x, y)) {
//                int[] points      = faceToPoints.get(face);
//                Float brightness  = faceToBrightness.get(face);
//
//                Color color = drawPointOfFace(gc, face, currentColor, brightness, points, x, y);
//                if (color != null) {
//                   currentColor = color;
//                   pointFound = true;
//                   if (entryInFaceList > 1) {
//                      // move the current face to the top of the list, so it will be more quickly found
//                      orderedFaces.remove(face);
//                      orderedFaces.add(0, face);
//                      // The for loop above is now modified, so it will throw a concurrent modification exception
//                      // if you don't exit this before before the end of the for's block.
//                   }
//                   // no need to check other faces:
//                   break;
//                }
//             }
//          }
//          if (!pointFound) {
//             pointFound = false;
//             Color color = getColor(gc, 0xff0000, .9f);
//             if (currentColor != color) {
//                gc.setForeground(color);
//                currentColor = color;
//             }
//             gc.drawPoint(x, y);
//          }
//       }
//    }
// }
}
