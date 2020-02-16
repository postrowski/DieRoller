package com.ostrowski.graphics.model;

/**
 * A simple tuple of 2 elements. A tuple is a set of values that relate
 * to each other in some way. In this case its a set of 2 so it might
 * represent a vertex or normal in 2D space or a texture coordinate
 *
 */
public class Tuple2 implements Cloneable, Comparable<Tuple2> {
	/** The x element in this tuple */
	private final float _x;
	/** The y element in this tuple */
	private final float _y;

	/**
	 * Create a new Tuple of 2 elements
	 *
	 * @param x The X element value
	 * @param y The Y element value
	 */
	public Tuple2(float x,float y) {
		this._x = x;
		this._y = y;
	}

	/**
	 * Get the X element value from this tuple
	 *
	 * @return The X element value from this tuple
	 */
	public float getX() {
		return _x;
	}

	/**
	 * Get the Y element value from this tuple
	 *
	 * @return The Y element value from this tuple
	 */
	public float getY() {
		return _y;
	}

   public Tuple2 add(float dx, float dy) {
      return new Tuple2(getX() + dx, getY() + dy);
   }
   public Tuple2 add(Tuple2 other) {
      return new Tuple2(getX() + other.getX(), getY() + other.getY());
   }
   public Tuple2 subtract(Tuple2 other) {
      return new Tuple2(getX() - other.getX(), getY() - other.getY());
   }
   public Tuple2 divide(float d) {
      return new Tuple2(getX() / d, getY() / d);
   }
   public Tuple2 multiply(float d) {
      return new Tuple2(getX() * d, getY() * d);
   }

   public float dotProduct(Tuple2 other) {
      return (getX() * other.getX()) + (getY() * other.getY());
   }
   public double magnitude() {
      return Math.sqrt(this.dotProduct(this));
   }

   public Tuple2 unitVector() {
      return divide((float) magnitude());
   }

   @Override
   public String toString() {
      return "Tuple2:{" + getX() + ", " + getY() + ")";
   }

   @Override
   public Tuple2 clone() {
      return new Tuple2(_x, _y);
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof Tuple2) {
         return compareTo((Tuple2) o) == 0;
      }
      return false;
   }

   @Override
   public int compareTo(Tuple2 o) {
      int comp;
      comp = compareAxis(_x, o._x, o); if (comp != 0) return comp;
      comp = compareAxis(_y, o._y, o); if (comp != 0) return comp;
      return 0;
   }

   public int compareAxis(float thisAxis, float othersAxis, Tuple2 other) {
      if (thisAxis == othersAxis) {
         return 0;
      }
      // If any axis is different, we should return the comparison based on the
      // magnitudes of the Tuple3s, rather than the individual axis'.
      double thisMag = magnitude();
      double otherMag = other.magnitude();
      if (thisMag != otherMag) {
         return Double.compare(thisMag, otherMag);
      }
      // If the magnitutes happen to be identical, compare the individual axis then:
      return Float.compare(thisAxis, othersAxis);
   }
}
