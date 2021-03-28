package com.ostrowski.graphics;

import java.util.ArrayList;
import java.util.List;

import com.ostrowski.graphics.model.ColoredFace;
import com.ostrowski.graphics.model.Face;
import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.Tuple3;

import static com.ostrowski.graphics.World3D.UP_VECTOR;

public class Model
{
   protected final ObjData data;
   protected final float scale;
   protected       Frame   frame;
   protected       int     baseRGB = 0x3030FF;                  // default color is light blue
   protected       boolean moving  = true;
   protected       Tuple3  centerMass;

   public Model(ObjData data, float scale, Integer baseRGB) {
      this.data = data;
      this.scale = scale;
      if (baseRGB != null) {
         this.baseRGB = baseRGB;
      }
      frame = new Frame(new Tuple3(600, 300, 300),  // location
                        new Tuple3(0, 30, 420),    // velocity, pixels per second
                        //new Tuple3(-350, 460, 50));   // unit vector is axis, magnitude is degrees per second
                        new Tuple3((float)(Math.random() * 1000f -500f),
                                    (float)(Math.random() * 1000f -500f),
                                    (float)(Math.random() * 1000f -500f)));   // unit vector is axis, magnitude is degrees per second

      System.out.println("New object. rotationalAxis: " + frame.rotationalAxis.toString());
      this.data.scale(scale, scale, scale);
      centerMass = this.data.getAveragePoint();
   }

   public void update(float elapsedTimeInSeconds, Tuple3 acceleration, float floorZValue) {

      if (!moving) {
         return;
      }
      Frame nextFrame = frame.update(elapsedTimeInSeconds, acceleration);

      // Check if a corner hit the floor by iterating through each face, and each vertex in each face,
      // applying the current rotation matrix to each point, and seeing if it's lower than the floor.

      List<Tuple3> pointsBelowFloor = new ArrayList<>();
      List<Tuple3> pointsNearFloor = new ArrayList<>();
      Tuple3 centerMassBelowFloor = new Tuple3(0,0,0);
      float lowestZ = 1000;
      StringBuilder sb = new StringBuilder();
      int v = 0;
      for (Tuple3 vertex : data.getVerts()) {
         if (sb.length() > 0) {
            sb.append("\n");
         }
         //Tuple3 positionedVertex = vertex.applyTransformation(_orientationTransform).add(_location);
         Tuple3 positionedVertex = nextFrame.positionVertex(vertex);
         float z = positionedVertex.getZ();
         sb.append(" v").append(v++)
           .append(":{").append(positionedVertex.getX())
           .append(",").append(positionedVertex.getY())
           .append(",").append(z).append("}");

         if (z < (floorZValue +2)) {
            pointsNearFloor.add(positionedVertex);
         }
         if (z < floorZValue) {
            pointsBelowFloor.add(positionedVertex);
            centerMassBelowFloor = centerMassBelowFloor.add(positionedVertex);
         }
         if (z < lowestZ) {
            lowestZ = z;
         }
      }

      if (pointsBelowFloor.isEmpty()) {
         // no collision, continue moving
         frame = nextFrame;
         return;
      }

      float boundDepth = floorZValue - lowestZ;
      if (boundDepth > 1) {
         // If this bound went deeper than 2 pixels, split the time in half, and recompute each 1/2 frame
         // this will cause recursion until we find the collision time frame within 1 pixels below the surface.
         update(elapsedTimeInSeconds/2f, acceleration, floorZValue);
         update(elapsedTimeInSeconds/2f, acceleration, floorZValue);
         return;
      }

      //System.out.println(sb.toString());
      centerMassBelowFloor = centerMassBelowFloor.divide(pointsBelowFloor.size());

      // move the object to the floor level
      Tuple3 locDelta = new Tuple3(0, 0, boundDepth);
      nextFrame = nextFrame.addLocation(locDelta);
      centerMassBelowFloor = centerMassBelowFloor.add(locDelta);

      boolean anyFaceBelowFloor = pointsNearFloor.size() > 2;
      Tuple3 positionedCenterMass = nextFrame.positionVertex(centerMass);

      // Check if we have stopped moving
      if (((nextFrame.velocity.magnitude() * dampeningFactor) < acceleration.magnitude()) &&
          (nextFrame.rotationalAxis.magnitude() < 40) && // degrees per second
          anyFaceBelowFloor) {
         moving = false;
         frame = nextFrame;
      }
      else {
         frame = bounce(nextFrame, centerMassBelowFloor, positionedCenterMass, elapsedTimeInSeconds, acceleration);
      }
   }

   static float dampeningFactor = 0.75f;
   static float friction = .15f;
   static float rotationalInteria = 30f;

   protected Frame bounce(Frame frame, Tuple3 positionedCenterMassAtFloor, Tuple3 positionedCenterMass,
                          float elapsedTimeInSeconds, Tuple3 acceleration) {
      // If we are already moving upwards, this is still the previous bounce.
      Tuple3 velocity = frame.velocity;
      if (velocity.getZ() > 0) {
         return frame;
      }

      Tuple3 pointToCenter = positionedCenterMass.subtract(positionedCenterMassAtFloor);
      // Acceleration is expressed in pixels per second / second, and
      // torque is expressed in radians per second,
      // so we need to multiple the crossProduct by elapsedTimeSecond, and also convert the torque into Radians.
      // we can do this in one step with the call to multiply((float)(Math.toRadians(elapsedTimeInSeconds)))
      Tuple3 newTorque = pointToCenter.crossProduct(acceleration)
                                      .multiply((float)(elapsedTimeInSeconds / rotationalInteria));
      // TODO: Since the torque is applied off-center, should there be a lateral force as well?


      // TODO: any existing lateral movement should be converted into rotational momentum by friction


      double percentBounce = pointToCenter.unitVector().dotProduct(UP_VECTOR);

      // If the object is rotating, some of that rotation should be turned in lateral movement:
      Tuple3 nextFramePositionedCenterMassAtFloor = positionedCenterMassAtFloor.subtract(frame.location)
                                                                               .applyTransformation(frame.orientationTransform)
                                                                               .add(frame.location);
      Tuple3 movementOfCenterMassAtFloor = nextFramePositionedCenterMassAtFloor.subtract(positionedCenterMassAtFloor);

      Tuple3 newVelocity = new Tuple3(velocity.getX() - movementOfCenterMassAtFloor.getX() * friction * -20,
                                      velocity.getY() - movementOfCenterMassAtFloor.getY() * friction * -20,
                                      (- velocity.getZ() * (float)percentBounce));
              //.multiply(dampeningFactor);

      Frame newFrame = frame.setVelocity(newVelocity);
      Tuple3 newRotationalAxis = addRotationalVectors(newFrame.rotationalAxis.multiply(1 - friction), newTorque);
      return newFrame.setRotationalAxis(newRotationalAxis);
   }
   public Tuple3 addRotationalVectors(Tuple3 rv1, Tuple3 rv2) {
      if ((rv1.magnitude() > 180) || (rv2.magnitude() > 180)) {
         return addRotationalVectors(rv1.multiply(.25f), rv2.multiply(.25f)).multiply(4f);
      }
      return rv1.add(rv2);
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


   /*
    * This method makes a copy of the Faces as ColoredFaces, in world positions with color.
    */
   public List<ColoredFace> getColoredFaces() {
      List<ColoredFace> faces = new ArrayList<>();
      for (Face face : data.getFaces()) {
         ColoredFace newFace = new ColoredFace(face, baseRGB);
         newFace.applyTransformation(frame.orientationTransform);
         newFace.move(frame.location);
         faces.add(newFace);
      }
      return faces;
   }

}
