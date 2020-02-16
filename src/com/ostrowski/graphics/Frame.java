package com.ostrowski.graphics;

import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.Tuple3;

import static com.ostrowski.graphics.World3D.UP_VECTOR;

public class Frame {
   public final Tuple3    _location;
   public final Tuple3    _velocity;
   public final Tuple3    _rotationalAxis;
   public final Matrix3x3 _orientationTransform;

   public Frame(Tuple3 location, Tuple3 velocity, Tuple3 rotationalAxis) {
      this (location, velocity, rotationalAxis, Matrix3x3.IdentityMatrix());
   }

   public Frame(Tuple3 location, Tuple3 velocity, Tuple3 rotationalAxis, Matrix3x3 orientationTransform) {
      _location             = location;
      _velocity             = velocity;
      _rotationalAxis       = rotationalAxis;
      _orientationTransform = orientationTransform;
   }

   public Frame update(float elapsedTimeInSeconds, Tuple3 acceleration) {

      // Finally, update the current velocity by applying the gravity:
      Tuple3 initialVelocity = _velocity;
      Tuple3 newVelocity = _velocity.add(acceleration.multiply(elapsedTimeInSeconds));
      Tuple3 averageVelocity = initialVelocity.add(newVelocity).divide(2.0f);
      Tuple3 newLocation = _location.add(averageVelocity.multiply(elapsedTimeInSeconds));
      Matrix3x3 newOrientationTransform = _orientationTransform;
      double rotationInDegrees = _rotationalAxis.magnitude() * elapsedTimeInSeconds;
      System.out.println("rotationInDegrees = " + rotationInDegrees);
      if (rotationInDegrees != 0) {
         Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(_rotationalAxis, rotationInDegrees);
         newOrientationTransform = rotationalMatrix.multiply(_orientationTransform);
      }

      System.out.println("location = " + newLocation);
      return new Frame(newLocation, newVelocity, _rotationalAxis, newOrientationTransform);
   }

   public Tuple3 positionVertex(Tuple3 vertex) {
      return vertex.applyTransformation(_orientationTransform).add(_location);
   }

   public Frame setVelocity(Tuple3 velocity) {
      return new Frame(_location, velocity, _rotationalAxis, _orientationTransform);
   }

   public Frame setRotationalAxis(Tuple3 rotationalAxis) {
      return new Frame(_location, _velocity, rotationalAxis, _orientationTransform);
   }
   public Frame setRotationalSpeed(float degreesPerSecond) {
      return setRotationalAxis(_rotationalAxis.unitVector().multiply(degreesPerSecond));
   }

   public Frame addLocation(Tuple3 locDelta) {
      return new Frame(_location.add(locDelta), _velocity, _rotationalAxis, _orientationTransform);
   }

   public Frame setUp(Tuple3 targetOrientation) {
      float rotationInDegrees = (float) Math.toDegrees(Math.acos(targetOrientation.dotProduct(UP_VECTOR)));
      Tuple3 rotationalAxis = targetOrientation.crossProduct(UP_VECTOR).unitVector();
      Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(rotationalAxis, rotationInDegrees);
      Matrix3x3 newOrientationTransform = rotationalMatrix.multiply(_orientationTransform);
      return new Frame(_location, _velocity, _rotationalAxis, newOrientationTransform);
   }
   public synchronized Frame rotateByDegrees(double rotationInDegrees) {
      Matrix3x3 rotationalMatrix = Matrix3x3.getRotationalTransformationAboutVector(_rotationalAxis, rotationInDegrees);
      Matrix3x3 newOrientationTransform = rotationalMatrix.multiply(_orientationTransform);
      return new Frame(_location, _velocity, _rotationalAxis, newOrientationTransform);
   }

}
