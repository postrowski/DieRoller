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
   protected       Tuple3    _location             = new Tuple3(600, 300, 100);
   protected       Tuple3    _velocity             = new Tuple3(0, 0, -10);   // per second
   protected       Tuple3    _rotationalAxis       = new Tuple3(-50, 60, 30);   // unit vector is axis, magnitude is degrees per second
   protected       int       _baseRGB              = 0x3030FF;                  // default color is light blue
   protected       Matrix3x3 _orientationTransform = Matrix3x3.IdentityMatrix();

   public Model(ObjData data, float scale, Integer baseRGB) {
      _data     = data;
      _scale    = scale;
      if (baseRGB != null) {
         _baseRGB = baseRGB;
      }
      _data.scale(scale, scale, scale);
   }

   public void update(float elapsedTimeInSeconds, Tuple3 acceleration, float floorZvalue) {

      // Finally, update the current velocity by applying the gravity:
      Tuple3 initialVelocity = _velocity;
      _velocity = _velocity.add(acceleration.multiply(elapsedTimeInSeconds));
      Tuple3 averageVelocity = initialVelocity.add(_velocity).divide(2.0f);
      _location = _location.add(averageVelocity.multiply(elapsedTimeInSeconds));

      System.out.println("location = " + _location);
      adjustRotation(elapsedTimeInSeconds);

      // Check if a corner hit the floor by iterating through each face, and each vertex in each face,
      // applying the current rotation matrix to each point, and seeing if it's lower than the floor.
      Tuple3 positonedBouncePoint = null;
      Tuple3 centerMass = new Tuple3(0,0,0);
      Float lowestZ = null;
      for (Face face : _data.getFaces()) {
         centerMass = centerMass.add(face.getVertexCenter());
         for (int v=0 ; v<face._vertexCount ; v++) {
            Tuple3 vertex = face.getVertex(v);
            Tuple3 positionedVertex = vertex.applyTransformation(_orientationTransform).add(_location);
            if (positionedVertex.getZ() < floorZvalue) {
               if ((lowestZ == null) || (lowestZ > positionedVertex.getZ())) {
                  positonedBouncePoint = positionedVertex;
                  lowestZ = positionedVertex.getZ();
               }
            }
         }
      }
      centerMass = centerMass.divide(_data.getFaceCount());
      Tuple3 positionedCenterMass = centerMass.add(_location);

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

   private synchronized void adjustRotation(double elapsedTimeInSeconds) {
      double rotationInDegrees = _rotationalAxis.magnitude() * elapsedTimeInSeconds;
      System.out.println("rotationInDegrees = " + rotationInDegrees);
      if (rotationInDegrees != 0) {
         Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(_rotationalAxis, rotationInDegrees);
         _orientationTransform = rotationalMatrix.multiply(_orientationTransform);
      }
   }

   protected void bounce(Tuple3 bouncePoint, Tuple3 positionedCenterMass, Tuple3 acceleration, float floorZvalue) {
      _velocity = new Tuple3(_velocity.getX() *  0.90f,
                             _velocity.getY() *  0.90f,
                             _velocity.getZ() * -0.75f);
   }

   /*
    * This method makes a copy of the Faces as ColoredFaces, in world positions with color.
    */
   public List<ColoredFace> getColoredFaces() {
      List<ColoredFace> faces = new ArrayList<>();
      for (Face face : _data.getFaces()) {
         ColoredFace newFace = new ColoredFace(face, _baseRGB);
         newFace.applyTransformation(_orientationTransform);
         newFace.move(_location);
         faces.add(newFace);
      }
      return faces;
   }

}
