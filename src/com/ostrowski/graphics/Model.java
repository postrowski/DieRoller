package com.ostrowski.graphics;

import java.util.ArrayList;
import java.util.List;

import com.ostrowski.graphics.model.ColoredFace;
import com.ostrowski.graphics.model.Face;
import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.Tuple3;

public class Model
{
   protected final ObjData   _data;
   protected final float     _scale;
   protected       Frame     _frame;
   protected       int       _baseRGB              = 0x3030FF;                  // default color is light blue

   public Model(ObjData data, float scale, Integer baseRGB) {
      _data     = data;
      _scale    = scale;
      if (baseRGB != null) {
         _baseRGB = baseRGB;
      }
      _frame = new Frame(new Tuple3(600, 300, 300),  // location
                         new Tuple3(0, 30, 420),    // velocity, pixels per second
                         //new Tuple3(-350, 460, 50));   // unit vector is axis, magnitude is degrees per second
                         new Tuple3((float)(Math.random() * 1000f -500f),
                                    (float)(Math.random() * 1000f -500f),
                                    (float)(Math.random() * 1000f -500f)));   // unit vector is axis, magnitude is degrees per second

      _data.scale(scale, scale, scale);
   }

   public void update(float elapsedTimeInSeconds, Tuple3 acceleration, float floorZvalue) {

      _frame = _frame.update(elapsedTimeInSeconds, acceleration);

      // Check if a corner hit the floor by iterating through each face, and each vertex in each face,
      // applying the current rotation matrix to each point, and seeing if it's lower than the floor.
      Tuple3 positonedBouncePoint = null;
      Tuple3 centerMass = new Tuple3(0,0,0);
      Float lowestZ = null;
      for (Face face : _data.getFaces()) {
         centerMass = centerMass.add(face.getVertexCenter());
         for (int v=0 ; v<face._vertexCount ; v++) {
            Tuple3 vertex = face.getVertex(v);
            //Tuple3 positionedVertex = vertex.applyTransformation(_orientationTransform).add(_location);
            Tuple3 positionedVertex = _frame.positionVertex(vertex);
            if (positionedVertex.getZ() < floorZvalue) {
               if ((lowestZ == null) || (lowestZ > positionedVertex.getZ())) {
                  positonedBouncePoint = positionedVertex;
                  lowestZ = positionedVertex.getZ();
               }
            }
         }
      }
      centerMass = centerMass.divide(_data.getFaceCount());
      Tuple3 positionedCenterMass = _frame.positionVertex(centerMass);

      if (positonedBouncePoint != null) {
         bounce(positonedBouncePoint, positionedCenterMass, acceleration, floorZvalue);
      }
   }
   public static Tuple3 getRotationToOrientNormalToZaxis(Tuple3 orientation) {
      // The rotation matrix will apply the X rotation first, followed by the Y rotation, followed by the Z rotation
      // We need to solve for only the X and Y, because once those have been applied, the orientation will already
      // be aligned to the Z axis.
      // Solve for the Y rotation first. To find the Y rotation, project the desired orientation on the XZ plane,
      // and determine the angle this makes to the Z axis.
      float angleY = (float) (Math.toDegrees(Math.atan2(0-orientation.getX(), orientation.getZ())));
      // now rotate the parameter into the Y-Z plane
      Matrix3x3 rotY = Matrix3x3.getRotationalTransformation(0, angleY, 0);
      Tuple3 orientationRotatedIntoYZPlane = orientation.applyTransformation(rotY);
      // and solve for the X rotation
      float angleX = (float) (Math.toDegrees(Math.atan2(orientationRotatedIntoYZPlane.getY(), orientationRotatedIntoYZPlane.getZ())));
      return new Tuple3(angleX, angleY, 0.0f);
   }

   protected void bounce(Tuple3 bouncePoint, Tuple3 positionedCenterMass, Tuple3 acceleration, float floorZvalue) {
      _frame = _frame.bounce(bouncePoint, positionedCenterMass, acceleration, floorZvalue);
   }

   /*
    * This method makes a copy of the Faces as ColoredFaces, in world positions with color.
    */
   public List<ColoredFace> getColoredFaces() {
      List<ColoredFace> faces = new ArrayList<>();
      for (Face face : _data.getFaces()) {
         ColoredFace newFace = new ColoredFace(face, _baseRGB);
         newFace.applyTransformation(_frame._orientationTransform);
         newFace.move(_frame._location);
         faces.add(newFace);
      }
      return faces;
   }

}
