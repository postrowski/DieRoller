package com.ostrowski.graphics.model;



/**
 * A single face defined on a model. This is a series of points
 * each with a position, normal and texture coordinate
 */
public class Face implements Cloneable {
    /** The vertices making up this face */
    protected final Tuple3[] _verts;
    /** The normals making up this face */
    protected final Tuple3[] _norms;
    /** The texture coordinates making up this face */
    protected final Tuple2[] _texs;
    /** The number of points */
    protected       int      _points = 0;
	 public    final int      _vertexCount;

	/**
	 * Create a new face
	 *
	 * @param points The number of points building up this face
	 */
	public Face(int points) {
	   _vertexCount = points;
		_verts = new Tuple3[points];
		_norms = new Tuple3[points];
		_texs  = new Tuple2[points];
	}

	/**
	 * Add a single point to this face
	 *
	 * @param vert The vertex location information for the point
	 * @param tex The texture coordinate information for the point
	 * @param norm the normal information for the point
	 */
	public void addPoint(Tuple3 vert, Tuple2 tex, Tuple3 norm) {
		_verts[_points] = vert;
		_texs[_points]  = tex;
		_norms[_points] = norm;

		_points++;
	}

	public Tuple3 getCommonNormal() {
      Tuple3 s01 = _verts[0].subtract(_verts[1]);
      Tuple3 s12 = _verts[1].subtract(_verts[2]);
      return s01.crossProduct(s12).normalize();
	}

	/**
	 * Get the vertex information for a specified point within this face.
	 *
	 * @param p The index of the vertex information to retrieve
	 * @return The vertex information from this face
	 */
	public Tuple3 getVertex(int p) {
		return _verts[p];
	}

	/**
	 * Get the texture information for a specified point within this face.
	 *
	 * @param p The index of the texture information to retrieve
	 * @return The texture information from this face
	 */
	public Tuple2 getTexCoord(int p) {
		return _texs[p];
	}

	/**
	 * Get the normal information for a specified point within this face.
	 *
	 * @param p The index of the normal information to retrieve
	 * @return The normal information from this face
	 */
	public Tuple3 getNormal(int p) {
		return _norms[p];
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
      if ((_verts.length == 3) || (_verts.length == 4)) {
//         int sourceIndex = 1;
//         int destIndex = (_verts.length -1);
//         Tuple3 tempT3 = _verts[sourceIndex];
//         _verts[sourceIndex] = _verts[destIndex];
//         _verts[destIndex] = tempT3;
//
//         tempT3 = _norms[sourceIndex];
//         _norms[sourceIndex] = _norms[destIndex];
//         _norms[destIndex] = tempT3;
//
//         Tuple2 tempT2 = _texs[sourceIndex];
//         _texs[sourceIndex] = _texs[destIndex];
//         _texs[destIndex] = tempT2;
         Face face = new Face(_vertexCount);
         face.addPoint(_verts[0], _texs[0], _norms[0]);
         if (_verts.length == 3) {
            face.addPoint(_verts[2], _texs[2], _norms[2]);
         }
         if (_verts.length == 4) {
            face.addPoint(_verts[3], _texs[3], _norms[3]);
            face.addPoint(_verts[2], _texs[2], _norms[2]);
         }
         face.addPoint(_verts[1], _texs[1], _norms[1]);
         return face;
      }
      throw new IllegalStateException();
   }

   public void applyTransformation(Matrix3x3 matrix) {
      for (int i=0 ; i<_verts.length ; i++) {
         _verts[i] = _verts[i].applyTransformation(matrix);
      }
      for (int i=0 ; i<_norms.length ; i++) {
         _norms[i] = _norms[i].applyTransformation(matrix);
      }
   }

   public void rotate(float x, float y, float z) {
      Matrix3x3 transform = Matrix3x3.getRotationalTransformation(x, y, z);
      applyTransformation(transform);
   }

   public void scale(double xScale, double yScale, double zScale) {
      for (int i=0 ; i<_verts.length ; i++) {
         _verts[i] = _verts[i].scale(xScale, yScale, zScale);
      }
      // we don't need to scale the texture map, because that is in its own scale [0-1).
   }

   public void move(Tuple3 offset) {
      for (int i=0 ; i<_verts.length ; i++) {
         _verts[i] = _verts[i].add(offset);
      }
   }

   public Tuple3 getVertexCenter() {
      Tuple3 vertexCenter = new Tuple3(0,0,0);
      for (int v=0 ; v<_vertexCount ; v++ ) {
         vertexCenter = vertexCenter.add(getVertex(v));
      }
      return vertexCenter.divide(_vertexCount);
   }

   @Override
   public Face clone() {
      Face dup = new Face(_points);
      for (int i=0 ; i<_points ; i++) {
         //dup.addPoint(_verts[i], _texs[i], _norms[i]);
         dup.addPoint(_verts[i].clone(), _texs[i].clone(), _norms[i].clone());
      }
      return dup;
   }
}