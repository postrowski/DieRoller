package com.ostrowski.graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;

import com.ostrowski.dieroller.TextureMapper;
import com.ostrowski.graphics.model.ColoredFace;
import com.ostrowski.graphics.model.Face;
import com.ostrowski.graphics.model.Matrix3x3;
import com.ostrowski.graphics.model.Tuple2;
import com.ostrowski.graphics.model.Tuple3;

public class World3D
{
   private final        List<Model>   _data                          = new ArrayList<>();
   private final        Tuple3        _acceleration                  = new Tuple3(0, 0, -1500);   // per seconds^2
   static final         Tuple3        _cameraPosition                = new Tuple3(0, 900, 900);   // the position of the camera
   final                Matrix3x3     _viewTransform                 = Matrix3x3./*IdentityMatrix();*/getRotationalTransformation(35, 0, 0); // in degrees
   private final static int           FOCAL_LENGTH                   = 2200;
   private final static int           CAMERA_DISTANCE                = 2500;
   private final        Tuple3        _lightSource                   = new Tuple3(0, 1, 1).unitVector();
   private final        Tuple3        _lightPerZ                     = _lightSource.divide(_lightSource.getZ());
   private final        int           _floorZValue                   = -100;
   private              Long          _timeOfLastUpdate              = null;
   private final static float         DARKEST_COLOR                  = 0.3f;
   private final static float         LIGHTEST_COLOR                 = 1.0f;
   private final        BufferedImage _texture;
   private              boolean       _waitingTillGetOutlineIsCalled = false;
   final                List<Face>    _shadows                       = new ArrayList<>();
   final                Tuple2        _shadowTextureVertex           = new Tuple2(0, 0);
   final                Tuple3        _shadowNormal                  = new Tuple3(0, 0, 1);
   long _totalTime = 0;
   long _drawCount = 0;
   public static final Tuple3 UP_VECTOR = new Tuple3(0, 0, 1);

   public void add(Model data) {
      _data.add(data);
   }
   public World3D(BufferedImage texture) {
      _texture       = texture;
   }

   public synchronized void update() {
      if (_waitingTillGetOutlineIsCalled) {
         return;
      }

//      if (_rollState == RollState.LEVELING) {
//         Tuple3 targetsCurrentOrientation = _targetOrientation.applyTransformation(_orientationTransform);
//         _rotationalAxis = targetsCurrentOrientation.crossProduct(_upVector).unitVector();
//         // set the rotational speed:
//         _rotationalAxis = _rotationalAxis.multiply(targetsCurrentOrientation.dotProduct(_upVector) * 5);
//      }

      long now = System.currentTimeMillis();
      if (_timeOfLastUpdate == null) {
         _timeOfLastUpdate = now;
         return;
      }
      float elapsedTimeInSeconds = (now - _timeOfLastUpdate) / 1000f;
      _timeOfLastUpdate = now;

      boolean debugging = false;
      if (debugging) {
         elapsedTimeInSeconds /= 10f;
      }

      for (Model model : _data) {
         model.update(elapsedTimeInSeconds, _acceleration, _floorZValue);
      }

      // Now that the location is finalized, figure out the shadows.
      computeShadows();

      _waitingTillGetOutlineIsCalled = true;
   }



//   private void setOrientationToNormal(Tuple3 orientation) {
//      setOrientation(getRotationToOrientNormalToZaxis(orientation));
//   }


   public void computeShadows() {
      _shadows.clear();
      for (Model model : _data) {
         for (Face face : model.getColoredFaces()) {
            Tuple3 normal = face.getCommonNormal();
            // See if the light is hitting this face.
            // If so, the clockwise orientation of the vertices will cast a clockwise shadow
            if (normal.dotProduct(_lightSource) > 0) {
               List<Tuple3> pointsOnFloor = new ArrayList<>();
               for (int v = 0 ; v<face._vertexCount ; v++) {
                  Tuple3 vertex = face.getVertex(v);
                  float distanceToFloor = _floorZValue - vertex.getZ();
                  Tuple3 pointOnFloor = _lightPerZ.multiply(distanceToFloor).add(vertex);
                  pointsOnFloor.add(pointOnFloor);
               }
               Face shadow = new Face(pointsOnFloor.size());
               for (Tuple3 point : pointsOnFloor) {
                  shadow.addPoint(point, _shadowTextureVertex, _shadowNormal);
               }
               _shadows.add(shadow);
            }
         }
      }
   }

   static Tuple3 adjustForViewAndPerspective(Tuple3 vertex, Matrix3x3 viewTransform) {
      Tuple3 adjusted = viewTransform.multiply(vertex);
      //float cameraDistance = (float) _cameraPosition.subtract(vertex).magnitude();
      float cameraDistance = CAMERA_DISTANCE - adjusted.getZ();
      float depthScale = FOCAL_LENGTH / cameraDistance;
      return adjusted.scale(depthScale, depthScale, 1.0);
   }
   public boolean isFacingAway(Face face) {
      Tuple3 vertex0 = adjustForViewAndPerspective(face.getVertex(0), _viewTransform);
      Tuple3 vertex1 = adjustForViewAndPerspective(face.getVertex(1), _viewTransform);
      Tuple3 vertex2 = adjustForViewAndPerspective(face.getVertex(2), _viewTransform);

      Tuple3 s01 = vertex0.subtract(vertex1);
      Tuple3 s12 = vertex1.subtract(vertex2);
      Tuple3 normal = s01.crossProduct(s12).normalize();
      return normal.getZ() < 0;
   }
   public synchronized Region getOutline() {
      Region region = new Region();
      for (Model model : _data) {
         for (Face face : model.getColoredFaces()) {
            // don't draw faces that face away:
            if (isFacingAway(face)) {
               continue;
            }
            region.add(getPoints(face));
         }
      }
      for (Face face : _shadows) {
         region.add(getPoints(face));
      }
      _waitingTillGetOutlineIsCalled = false;

      return region;
   }


   private int[] getPoints(Face face) {
      int offset = 0; //(int) ((_scale * -3)/ 2);
      int[] arry = new int[face._vertexCount * 2];
      int i=0;
      //StringBuilder sb = new StringBuilder();
      for (int v = 0 ; v<face._vertexCount ; v++) {
         Tuple3 tuple = adjustForViewAndPerspective(face.getVertex(v), _viewTransform);
         arry[i++] = Math.round(tuple.getX()) - offset;
         arry[i++] = Math.round(tuple.getY()) - offset;
//         sb.append("(").append(arry[i-2]).append(", ").append(arry[i-1]).append(") ");
      }
//      System.out.println(sb.toString());
      return arry;
   }

   public synchronized void paintControl(Display display, GC gc) {
      if ((display == null) || display.isDisposed() || (gc == null) || gc.isDisposed()) {
         return;
      }
      paintByFaces(gc);

      // Dispose of all the colors used:
      disposeColors();
   }

   private void paintByFaces(GC gc) {
      Color currentColor = getColor(gc, 0, 0);

      // Color the shadows first, they should always be farthest away:
      gc.setForeground(currentColor);
      gc.setBackground(currentColor);
      for (Face face : _shadows) {
         int[] points = getPoints(face);
         gc.fillPolygon(points);
      }

      // Order the faces so we draw the farthest away faces first
      List<ColoredFace> facesNotFacingAway = new ArrayList<>();
      for (Model model : _data) {
         facesNotFacingAway.addAll(model.getColoredFaces().stream()
                                        .filter(face -> !isFacingAway(face))
                                        .collect(Collectors.toList()));
      }
      facesNotFacingAway.sort((Comparator<Face>) (arg0, arg1) -> {
         float center0 = (_viewTransform.multiply(arg0.getVertexCenter())).getZ();
         float center1 = (_viewTransform.multiply(arg1.getVertexCenter())).getZ();
         return Float.compare(center0, center1);
      });

      // Then draw the faces:
      for (ColoredFace face : facesNotFacingAway) {
         Tuple3 normal = face.getCommonNormal();
         float brightness = _lightSource.dotProduct(normal);
         brightness = (Math.max(0, brightness) * (LIGHTEST_COLOR - DARKEST_COLOR)) + DARKEST_COLOR;

         int[] points = getPoints(face);
         Rectangle bounds = getBounds(points);

         int baseRGB = face.getColor();
         Color baseColor = getColor(gc, baseRGB, brightness);
         gc.setForeground(baseColor);
         gc.setBackground(baseColor);
         gc.fillPolygon(points);
         for (int y0 = Math.max(0, -bounds.y); y0 < bounds.height ; y0++) {
            int y = y0 + bounds.y;
            for (int x0 = Math.max(0, -bounds.x); x0 < bounds.width ; x0++) {
               int x = x0 + bounds.x;
               Color color = drawPointOfFace(gc, face, currentColor, baseRGB, brightness, points, x, y);
               if (color != null) {
                  currentColor = color;
               }
            }
         }
      }
   }

   private static Rectangle getBounds(int[] points) {
      int minX = points[0];
      int maxX = points[0];
      int minY = points[1];
      int maxY = points[1];
      for (int i=2 ; i<points.length ; i += 2) {
         minX = Math.min(minX, points[i]);
         maxX = Math.max(maxX, points[i]);
         minY = Math.min(minY, points[i+1]);
         maxY = Math.max(maxY, points[i+1]);
      }
      return new Rectangle(minX, minY, maxX - minX, maxY - minY);
   }

   private Color drawPointOfFace(GC gc, Face face, Color currentColor, int baseRGB, float brightness, int[] points, int x, int y) {
      Color color = getColorOfPointOnFace(gc, face, currentColor, baseRGB, brightness, points, x, y);
      if (color != null) {
         if (currentColor != color) {
            gc.setForeground(color);
         }
         gc.drawPoint(x, y);
      }
      return color;
   }
   private Color getColorOfPointOnFace(GC gc, Face face, Color currentColor, int baseRGB, float brightness, int[] points, int x, int y) {
      Tuple2 textureCoordCenter = getTextureCoord(face, points, x, y);
      if (textureCoordCenter != null) {
         float textureWidth  = 0;
         float textureHeight = 0;
         Tuple2 textureCoordOffsetX = getTextureCoord(face, points, x+1, y);
         if (textureCoordOffsetX != null) {
            textureWidth  = (textureCoordOffsetX.getX() - textureCoordCenter.getX()) * _texture.getWidth();
         }
         else {
            textureCoordOffsetX = getTextureCoord(face, points, x-1, y);
            if (textureCoordOffsetX != null) {
               textureWidth  = (textureCoordOffsetX.getX() - textureCoordCenter.getX()) * _texture.getWidth() * -1;
            }
         }
         Tuple2 textureCoordOffsetY = getTextureCoord(face, points, x, y+1);
         if (textureCoordOffsetY != null) {
            textureHeight = (textureCoordOffsetY.getY() - textureCoordCenter.getY()) * _texture.getHeight();
         }
         else {
            textureCoordOffsetY = getTextureCoord(face, points, x, y-1);
            if (textureCoordOffsetY != null) {
               textureHeight = (textureCoordOffsetY.getY() - textureCoordCenter.getY()) * _texture.getHeight() * -1;
            }
         }
         int xt = Math.round(textureCoordCenter.getX() * _texture.getWidth());
         int yt = Math.round(textureCoordCenter.getY() * _texture.getHeight());
         if ((xt > 0) && (yt > 0) && (xt<_texture.getWidth()) && (yt<_texture.getHeight())) {
            int textureRGB = _texture.getRGB( xt, yt) & 0x00ffffff;
            if ((textureWidth > 1.0f) || (textureHeight > 1.0f)) {
               int red   = 0;
               int green = 0;
               int blue  = 0;
               int count = 0;
               float stepSizeX = Math.max(0.2f, Math.abs(textureWidth / 10));
               float stepSizeY = Math.max(0.2f, Math.abs(textureHeight / 10));
               for (float x1 = 0-(textureWidth/2f) ; x1 < (textureWidth/2f) ; x1 += stepSizeX) {
                  for (float y1 = 0-(textureHeight/2f) ; y1 < (textureHeight/2f) ; y1 += stepSizeY) {
                     if (((x1+xt) > 0) && ((y1+yt) > 0) && (Math.round(x1 + xt)<_texture.getWidth()) && (Math.round(y1+ yt)<_texture.getHeight())) {
                        int textureRGB_ = _texture.getRGB( Math.round(x1 + xt), Math.round(y1 + yt)) & 0x00ffffff;
                        red   += (textureRGB_ >> 16) & 0xFF;
                        green += (textureRGB_ >>  8) & 0xFF;
                        blue  += (textureRGB_      ) & 0xFF;
                        count++;
                     }
                  }
               }
               if (count > 0) {
                  red   = red   / count;
                  green = green / count;
                  blue  = blue  / count;
                  textureRGB = (red << 16) | (green << 8) | blue;
               }
            }
            if (textureRGB != 0) {
               int red   = (textureRGB >> 16) & 0xFF;
               int green = (textureRGB >>  8) & 0xFF;
               int blue  = (textureRGB      ) & 0xFF;
               red   += (baseRGB >> 16) & 0xFF;
               green += (baseRGB >>  8) & 0xFF;
               blue  += (baseRGB      ) & 0xFF;

               textureRGB = (Math.min(0xFF, red) << 16) | (Math.min(0xFF, green) << 8) | Math.min(0xFF, blue);
               return getColor(gc, textureRGB, brightness);
            }
         }
      }
      return null;
   }

   private static Tuple2 getTextureCoord(Face face, int[] points, int x, int y) {
      if (isInside(points[0], points[1], points[2], points[3], points[4], points[5], x, y)) {
         return TextureMapper.getTextureMap(x, y,
                                            points[0], points[1], points[2], points[3], points[4], points[5],
                                            face.getTexCoord(0), face.getTexCoord(1), face.getTexCoord(2));
      }
      if (face._vertexCount == 4) {
         if (isInside(points[4], points[5], points[6], points[7], points[0], points[1], x, y)) {
            return TextureMapper.getTextureMap(x, y,
                                               points[4], points[5], points[6], points[7], points[0], points[1],
                                               face.getTexCoord(2), face.getTexCoord(3), face.getTexCoord(0));
         }
      }
      return null;
   }

   final HashMap<Integer, HashMap<Float, Color>> _colorsByRgbBrightness = new HashMap<>();
   private Color getColor(GC gc, int rgb, float brightness) {
      HashMap<Float, Color> colorsByBrightness = _colorsByRgbBrightness.computeIfAbsent(rgb, k -> new HashMap<>());
      Color color = colorsByBrightness.get(brightness);
      if (color == null) {
         color = new Color(gc.getDevice(),
                           Math.round( (rgb >> 16)         * brightness),
                           Math.round(((rgb >>  8) & 0xff) * brightness),
                           Math.round(( rgb        & 0xff) * brightness));
         colorsByBrightness.put(brightness, color);
      }
      return color;
   }
   final HashMap<Integer, HashMap<Integer, HashMap<Integer, Color>>> _colorsByRGB = new HashMap<>();

   private void disposeColors() {
      for (HashMap<Integer, HashMap<Integer, Color>> colorsByGB : _colorsByRGB.values()) {
         for (HashMap<Integer, Color> colorsByB : colorsByGB.values()) {
            for (Color color : colorsByB.values()) {
               color.dispose();
            }
            colorsByB.clear();
         }
         colorsByGB.clear();
      }
      _colorsByRGB.clear();
      for (HashMap<Float, Color> colorsByBrightness : _colorsByRgbBrightness.values()) {
         for (Color color : colorsByBrightness.values()) {
            color.dispose();
         }
         colorsByBrightness.clear();
      }
      _colorsByRgbBrightness.clear();
   }

   /* A utility function to calculate area of triangle formed by (x1, y1), (x2, y2) and (x3, y3) */
   private static double area(int x1, int y1, int x2, int y2, int x3, int y3)
   {
      return Math.abs(((x1*(y2-y3)) + (x2*(y3-y1))+ (x3*(y1-y2)))/2.0);
   }

   /* A function to check whether point P(x, y) lies inside the triangle formed
      by A(x1, y1), B(x2, y2) and C(x3, y3) */
   public static boolean isInsideByArea(int x1, int y1, int x2, int y2, int x3, int y3, int testX, int testY)
   {
      /* Calculate area of triangle ABC */
      double A = area(x1, y1, x2, y2, x3, y3);

      /* Calculate area of triangle PBC */
      double A1 = area(testX, testY, x2, y2, x3, y3);

      /* Calculate area of triangle PAC */
      double A2 = area(x1, y1, testX, testY, x3, y3);

      /* Calculate area of triangle PAB */
      double A3 = area(x1, y1, x2, y2, testX, testY);

      /* Check if sum of A1, A2 and A3 is same as A */
      return Math.abs(A - (A1 + A2 + A3)) < 1;
   }
   public static boolean isInside(int x1, int y1, int x2, int y2, int x3, int y3, int testX, int testY) {
      if (((x1==testX) && (y1==testY)) || ((x2==testX) && (y2==testY)) || ((x3==testX) && (y3==testY))) {
         return true;
      }
      // points are given in clockwise order.

      // Given: perpDotProduct(v1, v2) = v1x * v2y - v1y * v2x
      // check the perpDotProduct of (vector(1->2)) to vector(1->test)). If it's negative, we are outside
      if ((((x2-x1)*(testY-y1)) - ((y2-y1)*(testX-x1))) < 0) {
         return false;
      }
      // check the perpDotProduct of (vector(2->3)) to vector(2->test)). If it's negative, we are outside
      if ((((x3-x2)*(testY-y2)) - ((y3-y2)*(testX-x2))) < 0) {
         return false;
      }
      // check the perpDotProduct of (vector(3->1)) to vector(3->test)). If it's negative, we are outside
      if ((((x1-x3)*(testY-y3)) - ((y1-y3)*(testX-x3))) < 0) {
         return false;
      }
      // otherwise, we must be inside
      return true;
   }

}
