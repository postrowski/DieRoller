package com.ostrowski.graphics.model;

public class Matrix3x3 {
   public static Matrix3x3 IdentityMatrix() { return new Matrix3x3(new Tuple3(1,0,0),
                                                                   new Tuple3(0,1,0),
                                                                   new Tuple3(0,0,1)); }

   final Tuple3[] _row = new Tuple3[3];
   public Matrix3x3(Tuple3 row0, Tuple3 row1, Tuple3 row2) {
      _row[0] = row0;
      _row[1] = row1;
      _row[2] = row2;
   }
   public Matrix3x3 transpose() {
      Tuple3 newRow0 = new Tuple3(_row[0]._x, _row[1]._x, _row[2]._x);
      Tuple3 newRow1 = new Tuple3(_row[0]._y, _row[1]._y, _row[2]._y);
      Tuple3 newRow2 = new Tuple3(_row[0]._z, _row[1]._z, _row[2]._z);
      return new Matrix3x3(newRow0, newRow1, newRow2);
   }

   public Matrix3x3 multiply(Matrix3x3 m) {
      Matrix3x3 mT = m.transpose();
      Tuple3 newRow0 = new Tuple3(mT._row[0].dotProduct(_row[0]), mT._row[1].dotProduct(_row[0]), mT._row[2].dotProduct(_row[0]));
      Tuple3 newRow1 = new Tuple3(mT._row[0].dotProduct(_row[1]), mT._row[1].dotProduct(_row[1]), mT._row[2].dotProduct(_row[1]));
      Tuple3 newRow2 = new Tuple3(mT._row[0].dotProduct(_row[2]), mT._row[1].dotProduct(_row[2]), mT._row[2].dotProduct(_row[2]));
      return new Matrix3x3(newRow0, newRow1, newRow2);
   }

   public Tuple3 multiply(Tuple3 t) {
      return new Tuple3(t.dotProduct(_row[0]), t.dotProduct(_row[1]), t.dotProduct(_row[2]));
   }

   /* Computes a rotation matrix to rotates a point in space first about the x axis by the value of the first parameter,
    * then about the Y-axis by the value of the second parameter, and then about the Z-axis by the 3rd.
    * All parameters must be in Degrees.
    */
   public static Matrix3x3 getRotationalTransformation(float xRotation, float yRotation, float zRotation) {
      float cx = (float) Math.cos(Math.toRadians(xRotation));
      float cy = (float) Math.cos(Math.toRadians(yRotation));
      float cz = (float) Math.cos(Math.toRadians(zRotation));
      float sx = (float) Math.sin(Math.toRadians(xRotation));
      float sy = (float) Math.sin(Math.toRadians(yRotation));
      float sz = (float) Math.sin(Math.toRadians(zRotation));
      Matrix3x3 rotX = new Matrix3x3(new Tuple3( 1f, 0f, 0f),
                                     new Tuple3( 0f, cx,-sx),
                                     new Tuple3( 0f, sx, cx));
      Matrix3x3 rotY = new Matrix3x3(new Tuple3( cy, 0f, sy),
                                     new Tuple3( 0f, 1f, 0f),
                                     new Tuple3(-sy, 0f, cy));
      Matrix3x3 rotZ = new Matrix3x3(new Tuple3( cz,-sz, 0f),
                                     new Tuple3( sz, cz, 0f),
                                     new Tuple3( 0f, 0f, 1f));
      return rotX.multiply(rotY).multiply(rotZ);
   }

   public static Matrix3x3 getRotationalTransformationAboutVector(Tuple3 vector, double angleInDegrees) {
      float angle = (float) Math.toRadians(angleInDegrees);
      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      Tuple3 uv = vector.unitVector();
      float Ux = uv.getX();
      float Uy = uv.getY();
      float Uz = uv.getZ();

      //The math for this come from: https://en.wikipedia.org/wiki/Rotation_matrix
      return new Matrix3x3(new Tuple3((float)((Ux*Ux*(1-cosAngle)) + cosAngle),      (float)((Ux*Uy*(1-cosAngle)) - (Uz*sinAngle)), (float)((Ux*Uz*(1-cosAngle)) + (Uy*sinAngle))),
                           new Tuple3((float)((Uy*Ux*(1-cosAngle)) + (Uz*sinAngle)), (float)((Uy*Uy*(1-cosAngle)) + cosAngle),      (float)((Uy*Uz*(1-cosAngle)) - (Ux*sinAngle))),
                           new Tuple3((float)((Uz*Ux*(1-cosAngle)) - (Uy*sinAngle)), (float)((Uz*Uy*(1-cosAngle)) + (Ux*sinAngle)), (float)((Uz*Uz*(1-cosAngle)) + cosAngle))
                           );
   }
}

