package com.ostrowski.graphics;

import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.Tuple3;

import static com.ostrowski.graphics.World3D.UP_VECTOR;

public class Frame {
   public final Tuple3    location;
   public final Tuple3    velocity;
   public final Tuple3    rotationalAxis;
   public final Matrix3x3 orientationTransform;

   public Frame(Tuple3 location, Tuple3 velocity, Tuple3 rotationalAxis) {
      this (location, velocity, rotationalAxis, Matrix3x3.IdentityMatrix());
   }

   public Frame(Tuple3 location, Tuple3 velocity, Tuple3 rotationalAxis, Matrix3x3 orientationTransform) {
      this.location = location;
      this.velocity = velocity;
      this.rotationalAxis = rotationalAxis;
      this.orientationTransform = orientationTransform;
   }

   public Frame update(float elapsedTimeInSeconds, Tuple3 acceleration) {

      // Finally, update the current velocity by applying the gravity:
      Tuple3 initialVelocity = velocity;
      Tuple3 newVelocity = velocity.add(acceleration.multiply(elapsedTimeInSeconds));
      Tuple3 averageVelocity = initialVelocity.add(newVelocity).divide(2.0f);
      Tuple3 newLocation = location.add(averageVelocity.multiply(elapsedTimeInSeconds));
      Matrix3x3 newOrientationTransform = orientationTransform;
      double rotationInDegrees = rotationalAxis.magnitude() * elapsedTimeInSeconds;
      System.out.println("rotationInDegrees = " + rotationInDegrees);
      if (rotationInDegrees != 0) {
         Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(rotationalAxis, rotationInDegrees);
         newOrientationTransform = rotationalMatrix.multiply(orientationTransform);
      }

      System.out.println("location = " + newLocation);
      return new Frame(newLocation, newVelocity, rotationalAxis, newOrientationTransform);
   }

   public Tuple3 positionVertex(Tuple3 vertex) {
      return vertex.applyTransformation(orientationTransform).add(location);
   }

   public Frame setVelocity(Tuple3 velocity) {
      return new Frame(location, velocity, rotationalAxis, orientationTransform);
   }

   public Frame setRotationalAxis(Tuple3 rotationalAxis) {
      return new Frame(location, velocity, rotationalAxis, orientationTransform);
   }
   public Frame setRotationalSpeed(float degreesPerSecond) {
      return setRotationalAxis(rotationalAxis.unitVector().multiply(degreesPerSecond));
   }

   public Frame addLocation(Tuple3 locDelta) {
      return new Frame(location.add(locDelta), velocity, rotationalAxis, orientationTransform);
   }

   public Frame setUp(Tuple3 targetOrientation) {
      float rotationInDegrees = (float) Math.toDegrees(Math.acos(targetOrientation.dotProduct(UP_VECTOR)));
      Tuple3 rotationalAxis = targetOrientation.crossProduct(UP_VECTOR).unitVector();
      Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(rotationalAxis, rotationInDegrees);
      Matrix3x3 newOrientationTransform = rotationalMatrix.multiply(orientationTransform);
      return new Frame(location, velocity, this.rotationalAxis, newOrientationTransform);
   }
   public synchronized Frame rotateByDegrees(double rotationInDegrees) {
      Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(rotationalAxis, rotationInDegrees);
      Matrix3x3 newOrientationTransform = rotationalMatrix.multiply(orientationTransform);
      return new Frame(location, velocity, rotationalAxis, newOrientationTransform);
   }

}
