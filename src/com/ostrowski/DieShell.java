package com.ostrowski;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ostrowski.graphics.model.Tuple3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ostrowski.dieroller.Die;
import com.ostrowski.graphics.World3D;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.ObjLoader;

public class DieShell extends Thread implements PaintListener
{
   public Region region;
   public Shell  shell;
   public Canvas doubleBufferCanvas;
   GC    imageGC = null;
   Image image   = null;
   private final World3D world;

   static final Map<Integer, ObjData> DIE_MAP  = new HashMap<>();
   static BufferedImage               TEXTURE  = null;

   static {
      try {
         DIE_MAP.put( 4, ObjLoader.loadObj("res/dice/d4.obj"));
         DIE_MAP.put( 6, ObjLoader.loadObj("res/dice/d6.obj"));
         DIE_MAP.put( 8, ObjLoader.loadObj("res/dice/d8.obj"));
         DIE_MAP.put(10, ObjLoader.loadObj("res/dice/d10.obj"));
         DIE_MAP.put(12, ObjLoader.loadObj("res/dice/d12.obj"));
         DIE_MAP.put(99, ObjLoader.loadObj("res/dice/d99.obj"));
         DIE_MAP.put(20, ObjLoader.loadObj("res/dice/d20.obj"));
         TEXTURE = ObjLoader.loadImage("res/dice/diceMapLettersOnBlack.png");
      } catch (IOException e) {
         e.printStackTrace();
      }
   }


   public DieShell(Display display, String arg, float scale, Integer baseRgb)
   {
      System.out.println("Creating new die Shell");

      // =========================================
      // Create a Shell (window) from the Display
      // =========================================
      shell = new Shell(display, SWT.NO_TRIM | SWT.DOUBLE_BUFFERED);// | SWT.ON_TOP);

      shell.setLayout(new FillLayout());

      doubleBufferCanvas = new Canvas(shell, SWT.NO_BACKGROUND);
      doubleBufferCanvas.addPaintListener(this);

      if (!arg.startsWith("d")) {
         throw new InvalidParameterException();
      }
      String sideStr = arg.substring(1);
      Integer results = null;
      int equalLoc = arg.indexOf("=");
      if (equalLoc != -1) {
         sideStr = arg.substring(1, equalLoc);
         String resultsStr = arg.substring(equalLoc + 1);
         if (resultsStr.equals("+")) {
            resultsStr = "12";
         }
         if (resultsStr.equals("-")) {
            resultsStr = "11";
         }
         results = Integer.parseInt(resultsStr);
      }
      int sides = Integer.parseInt(sideStr);
      if (results == null) {
         results = (int)(Math.floor(Math.random() * sides));
      }
      world = new World3D(TEXTURE);
      //_world.add(new Die(sides, DIE_MAP.get(sides).clone(), scale, results, baseRgb, null));
      List<Tuple3> directions = new ArrayList<>();
      directions.add(new Tuple3(61,42,307));// per second
      //_world.add(new Die(12, DIE_MAP.get(99).clone(), scale, 3, baseRgb, directions));
      directions.clear();
      directions.add(new Tuple3(-61,-42,307));// per second
      //_world.add(new Die(4, DIE_MAP.get(4).clone(), scale, 3, baseRgb, directions));
      world.add(new Die(6, DIE_MAP.get(6).clone(), scale, 3, baseRgb, directions));

      shell.setSize(100, 100);
      shell.open();

      // =============================================================
      // Register a listener for the Close event on the die Shell.
      // This disposes the Die Shell
      // =============================================================
      shell.addListener(SWT.Close, event -> {
         System.out.println("Die Shell handling Close event, about to dispose this Shell");
         shell.dispose();
      });

      start();
   }

   @Override
   public void run() {
      while (!shell.isDisposed()) {
         try {
            Thread.sleep(16);

         } catch (InterruptedException e) {
            break;
         }

         if (shell.isDisposed()) {
            break;
         }
         world.update();

         shell.getDisplay().asyncExec(() -> {
            if (region != null) {
               region.dispose();
            }
            region = world.getOutline();

            if (!shell.isDisposed()) {
               image = paintImage(shell.getDisplay());
               if ((region != null) && (!region.isDisposed())) {
                  shell.setRegion(region);
                  Rectangle size = region.getBounds();
                  // clip the height & width to [1, 100]
                  size.height = Math.min(Math.max(size.height, 1), 400);
                  size.width = Math.min(Math.max(size.width, 1), 400);
                  //// add 25 to each dimension to prevent clipping
                  size.width += 25;
                  size.height += 25;
                  shell.setSize(size.width + size.x, size.height + size.y);
               }
            }
         });
      }
   }


   @Override
   public void paintControl(PaintEvent event) {
      // Draws the buffer image onto the canvas.
      if ((image != null) && (!image.isDisposed())) {
         event.gc.drawImage(image, 0, 0);
      }

      if (imageGC != null) {
         imageGC.dispose();
         imageGC = null;
      }
   }

   Image paintImage(Display display) {
      synchronized (world) {

         // Creates new image only when absolutely necessary.
         Point canvasSize = doubleBufferCanvas.getSize();
         int width = canvasSize.x;
         int height = canvasSize.y;
         if ((width > 0) && (height > 0)) {
//          Image image = (Image) doubleBufferCanvas.getData("double-buffer-image");
//            if (   (image == null)
//                || (image.getBounds().width  < canvasSize.x)
//                || (image.getBounds().height < canvasSize.y))
            {
               if (image != null) {
                  width  = Math.max(width, image.getBounds().width);
                  height = Math.max(height, image.getBounds().height);
                  image.dispose();
               }
//               // Add 10 to the image size to reduce the chance it needs to be recreated
//               width += 10; height += 10;

//               System.out.println("creating image (" + width + ", " + height + ")");
               image = new Image(display, width, height);
//               doubleBufferCanvas.setData("double-buffer-image", image);
            }
         }

         // Initializes the graphics context of the image.
         try {
            imageGC = new GC(image);
            world.paintControl(display, imageGC);
         }
         catch (IllegalArgumentException e) {
            System.out.println(e);
         }
         return image;
      }
   }
}