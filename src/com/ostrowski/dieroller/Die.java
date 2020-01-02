package com.ostrowski.dieroller;


import static com.ostrowski.graphics.World3D.UP_VECTOR;

import java.util.List;

import com.ostrowski.graphics.Model;
import com.ostrowski.graphics.model.ColoredFace;
import com.ostrowski.graphics.model.Face;
import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.Tuple3;

public class Die extends Model
{
   private final int          _resultRGB = 0xA03030; // default results color is redish
   private final int          _results;
   private final Tuple3       _targetOrientation;
   private final List<Tuple3> _directions;
   protected     RollState    _rollState = RollState.DROPPING;

   public Die(int sides, ObjData data, float scale, int results, Integer baseRGB, List<Tuple3> directions) {
      super(data, scale, baseRGB);
      _results = results;
      _directions = directions;

      // override the default direction with the first direction
      if ((_directions != null) && (!_directions.isEmpty())) {
         _velocity = _directions.remove(0);
      }

      Face face = _data.getFace(_results - 1);
      Tuple3 normal = face.getCommonNormal();
      if (sides == 4) {
         normal = normal.multiply(-1.0f);
      }

      _targetOrientation = normal;
      if (sides == 20) {
         _velocity.add(new Tuple3(10, -40, 0));
      }

      // Orient the die so that it starts out with the results face up
      float rotationInDegrees = (float) Math.toDegrees(Math.acos(_targetOrientation.dotProduct(UP_VECTOR)));
      Tuple3 rotationalAxis = _targetOrientation.crossProduct(UP_VECTOR).unitVector();
      Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(rotationalAxis, rotationInDegrees);
      _orientationTransform = rotationalMatrix.multiply(_orientationTransform);
   }

   @Override
   public void update(float elapsedTimeInSeconds, Tuple3 acceleration, float floorZvalue) {
      if (_rollState == RollState.STOPPED) {
         return;
      }
      super.update(elapsedTimeInSeconds, acceleration, floorZvalue);
   }

   @Override
   protected void bounce(Tuple3 bouncePoint, Tuple3 positionedCenterMass, Tuple3 acceleration, float floorZvalue) {
      // If we are already moving upwards, this is still the previous bounce.
      if (_velocity.getZ() > 0) {
         return;
      }

      // TODO: use the vectors in the _directions list for each bounce.
//      if ((_directions != null) && (!_directions.isEmpty())) {
//         _velocity = _directions.remove(0);
//      }

      float bounceDepth = floorZvalue - bouncePoint.getZ();
      _location = _location.add(0, 0, (1.5f * bounceDepth));
      Tuple3 pointToCenter = positionedCenterMass.subtract(bouncePoint);
      _rollState = _rollState.getNextState();
      Tuple3 targetsCurrentOrientation = _targetOrientation.applyTransformation(_orientationTransform);
      float rotationInDegrees = (float) Math.toDegrees(Math.acos(targetsCurrentOrientation.unitVector().dotProduct(UP_VECTOR)));
      if (_rollState == RollState.STOPPED) {
         _rotationalAxis = targetsCurrentOrientation.crossProduct(UP_VECTOR).unitVector();
         Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(_rotationalAxis, rotationInDegrees);
         _orientationTransform = rotationalMatrix.multiply(_orientationTransform);

         _velocity = new Tuple3(0,0,0);
         _rotationalAxis = new Tuple3(0,0,0);

         float lowestZ = 0;
         for (Face face : _data.getFaces()) {
            Tuple3 faceCenter = face.getVertexCenter();
            Tuple3 positionedVertex = faceCenter.applyTransformation(_orientationTransform).add(_location);

            if (lowestZ > positionedVertex.getZ()) {
               lowestZ = positionedVertex.getZ();
            }
         }
         _location = _location.add(0, 0, floorZvalue - lowestZ);
         return;
      }

      if ((_rollState == RollState.LEVELING) || (_rollState == RollState.BOUNCE_4)) {
         _location = _location.add(0, 0, bounceDepth);
         _rotationalAxis = targetsCurrentOrientation.crossProduct(UP_VECTOR).unitVector();

         _velocity = new Tuple3(_velocity.getX() *  0.25f,
                                _velocity.getY() *  0.25f,
                                (float)(Math.max((_velocity.getZ() * -0.1f), 0.3)));
         if (_rollState == RollState.BOUNCE_4) {
            _velocity = new Tuple3(  5,  5,  10);
         }
         float timeToBounce = Math.abs(_velocity.getZ() / acceleration.getZ()) * 2.0f;

         float degreePerSeconds = rotationInDegrees / timeToBounce;
         // set the rotational speed:
         System.out.println("Leveling: axis=" + _rotationalAxis + ", angle=" + rotationInDegrees + ", timeToBounce=" + timeToBounce);
         _rotationalAxis = _rotationalAxis.multiply(degreePerSeconds);
         return;
      }
      switch (_rollState) {
         case BOUNCE_1:  _velocity = new Tuple3( 40, 50, 300);  _rotationalAxis = new Tuple3(-500,  60, 150); return;
         case BOUNCE_2:  _velocity = new Tuple3(-20, 10, 100);  _rotationalAxis = new Tuple3(-300,  40, 100); return;
         case BOUNCE_3:  _velocity = new Tuple3(-10,-10,  30);  _rotationalAxis = new Tuple3(-250,  60, -50); return;
         case BOUNCE_4:  _velocity = new Tuple3(  5,  5,  10);  _rotationalAxis = new Tuple3( 100, 120, -20); return;
         default:
      }
      _velocity = new Tuple3((_velocity.getX() *  0.5f) + ((pointToCenter.getX() * 3.0f)/3f),
                             (_velocity.getY() *  0.5f) + ((pointToCenter.getY() * 3.0f)/3f),
                             (_velocity.getZ() * -0.4f) + ((pointToCenter.getZ() * 0.5f)/3f));

      if ((_rollState == RollState.BOUNCE_1) || (_rollState == RollState.BOUNCE_2)) {
         _rotationalAxis = _rotationalAxis.add(pointToCenter.crossProduct(UP_VECTOR).unitVector());
         return;
      }

      float timeToBounce = Math.abs(_velocity.getZ() / acceleration.getZ()) * 2.0f;
      float degreePerSeconds = rotationInDegrees / timeToBounce;
//      while (Math.abs(rotationInDegrees) > 360f) {
//         rotationInDegrees -= 360 * Math.signum(rotationInDegrees);
//         degreePerSeconds = rotationInDegrees / timeToBounce;
//      }
      // set the rotational speed:
      if (Math.abs(degreePerSeconds) < Math.abs(_rotationalAxis.magnitude())) {
         // set the rotational speed:
         degreePerSeconds = (float) Math.toDegrees(Math.acos(pointToCenter.unitVector().dotProduct(UP_VECTOR)));
      }
      _rotationalAxis = targetsCurrentOrientation.crossProduct(UP_VECTOR).unitVector().multiply(degreePerSeconds);
   }

   @Override
   public List<ColoredFace> getColoredFaces() {
      List<ColoredFace> faces = super.getColoredFaces();
      // augment the results face by shading in a contrasting color
      if (_rollState == RollState.STOPPED) {
         for (ColoredFace face : faces) {
            float upness = face.getCommonNormal().dotProduct(UP_VECTOR);
            if (upness > 0.99f) {
               face.setColor(_resultRGB);
               break;
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
//    for (Face face : _data.getFaces()) {
//       // don't draw faces that face away:
//       if (isFacingAway(face)) {
//          continue;
//       }
//
//       float brightness = _lightSource.dotProduct(normal);
//       brightness = (Math.max(0, brightness) * (_lightestColor - _darkestColor)) + _darkestColor;
//
//       int[] points = getPoints(face);
//       faceToRect.put(face, getBounds(points));
//       faceToBrightness.put(face, brightness);
//       faceToPoints.put(face, points);
//       orderedFaces.add(face);
//    }
//
////    _image_red   = new int[area.width][area.height];
////    _image_green = new int[area.width][area.height];
////    _image_blue  = new int[area.width][area.height];
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
