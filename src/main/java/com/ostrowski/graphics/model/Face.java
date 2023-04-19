package com.ostrowski.graphics.model;


import java.util.Arrays;

/**
 * A single face defined on a model. This is a series of points
 * each with a position, normal and texture coordinate
 */
public class Face implements Cloneable {
   /** The vertices making up this face */
   protected final Tuple3[] verts;
   /** The normals making up this face */
   protected final Tuple3[] norms;
   /** The texture coordinates making up this face */
   protected final Tuple2[] texs;
   private final String     label;
   /** The number of points */
   protected       int      points = 0;
   public    final int      vertexCount;

   /**
    * Create a new face
    *
    * @param points The number of points building up this face
    */
   public Face(int points, String label) {
      vertexCount = points;
      verts = new Tuple3[points];
      norms = new Tuple3[points];
      texs = new Tuple2[points];
      this.label = label;
   }

   /**
    * Add a single point to this face
    *
    * @param vert The vertex location information for the point
    * @param tex The texture coordinate information for the point
    * @param norm the normal information for the point
    */
   public void addPoint(Tuple3 vert, Tuple2 tex, Tuple3 norm) {
      verts[points] = vert;
      texs[points]  = tex;
      norms[points] = norm;

      points++;
   }

   public Tuple3 getCommonNormal() {
      Tuple3 s01 = verts[0].subtract(verts[1]);
      Tuple3 s12 = verts[1].subtract(verts[2]);
      return s01.crossProduct(s12).normalize();
   }

   /**
    * Get the vertex information for a specified point within this face.
    *
    * @param p The index of the vertex information to retrieve
    * @return The vertex information from this face
    */
   public Tuple3 getVertex(int p) {
      return verts[p];
   }

   /**
    * Get the texture information for a specified point within this face.
    *
    * @param p The index of the texture information to retrieve
    * @return The texture information from this face
    */
   public Tuple2 getTexCoord(int p) {
      return texs[p];
   }

   /**
    * Get the normal information for a specified point within this face.
    *
    * @param p The index of the normal information to retrieve
    * @return The normal information from this face
    */
   public Tuple3 getNormal(int p) {
      return norms[p];
   }

   public String getLabel() {
      return label;
   }
   /**
    * change the order of the points on the triangle from A->B->C    to A->C->B
    *                           or the quadrilateral from A->B->C->D to A->D->C->B
    * by swapping the second point with the last point.
    * So if a normal is recomputed it will be reversed 180 degrees:
    *     A   =>   A       |       A D  =>   A B
    *    B C  =>  C B      |       B C  =>   D C
    */
   public Face invertNormal() {
      if ((verts.length == 3) || (verts.length == 4)) {
//         int sourceIndex = 1;
//         int destIndex = (verts.length -1);
//         Tuple3 tempT3 = verts[sourceIndex];
//         verts[sourceIndex] = verts[destIndex];
//         verts[destIndex] = tempT3;
//
//         tempT3 = norms[sourceIndex];
//         norms[sourceIndex] = norms[destIndex];
//         norms[destIndex] = tempT3;
//
//         Tuple2 tempT2 = texs[sourceIndex];
//         texs[sourceIndex] = texs[destIndex];
//         texs[destIndex] = tempT2;
         Face face = new Face(vertexCount, label);
         face.addPoint(verts[0], texs[0], norms[0]);
         if (verts.length == 3) {
            face.addPoint(verts[2], texs[2], norms[2]);
         }
         if (verts.length == 4) {
            face.addPoint(verts[3], texs[3], norms[3]);
            face.addPoint(verts[2], texs[2], norms[2]);
         }
         face.addPoint(verts[1], texs[1], norms[1]);
         return face;
      }
      throw new IllegalStateException();
   }

   public void applyTransformation(Matrix3x3 matrix) {
      for (int i = 0; i < verts.length ; i++) {
         verts[i] = verts[i].applyTransformation(matrix);
      }
      for (int i = 0; i < norms.length ; i++) {
         norms[i] = norms[i].applyTransformation(matrix);
      }
   }

   public void rotate(float x, float y, float z) {
      Matrix3x3 transform = Matrix3x3.getRotationalTransformation(x, y, z);
      applyTransformation(transform);
   }

   public void scale(double xScale, double yScale, double zScale) {
      for (int i = 0; i < verts.length ; i++) {
         verts[i] = verts[i].scale(xScale, yScale, zScale);
      }
      // we don't need to scale the texture map, because that is in its own scale [0-1).
   }

   public void move(Tuple3 offset) {
      for (int i = 0; i < verts.length ; i++) {
         verts[i] = verts[i].add(offset);
      }
   }

   public Tuple3 getVertexCenter() {
      Tuple3 vertexCenter = new Tuple3(0,0,0);
      for (int v = 0; v < vertexCount; v++ ) {
         vertexCenter = vertexCenter.add(getVertex(v));
      }
      return vertexCenter.divide(vertexCount);
   }

   @Override
   public Face clone() {
      Face dup = new Face(points, label);
      for (int i = 0; i < points; i++) {
         //dup.addPoint(verts[i], texs[i], norms[i]);
         dup.addPoint(verts[i].clone(), texs[i].clone(), norms[i].clone());
      }
      return dup;
   }

   @Override
   public String toString() {
      return this.getClass().getName() + "{" +
             "label='" + label + '\'' +
             ", verts=" + Arrays.toString(verts) +
             ", norms=" + Arrays.toString(norms) +
             ", texs=" + Arrays.toString(texs) +
             ", points=" + points +
             ", vertexCount=" + vertexCount +
             '}';
   }
}