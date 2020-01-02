package com.ostrowski;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.ostrowski.dieroller.Die;
import com.ostrowski.graphics.World3D;
import com.ostrowski.graphics.model.ObjData;
import com.ostrowski.graphics.model.ObjLoader;

public class DieShell extends Thread implements PaintListener
{
   public Region                      _region;
   public Shell                       _shell;
   public Canvas                      _doubleBufferCanvas;
   GC                                 _imageGC = null;
   Image                              _image   = null;
   private final World3D _world;

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
      _shell = new Shell(display, SWT.NO_TRIM | SWT.DOUBLE_BUFFERED);// | SWT.ON_TOP);

      _shell.setLayout(new FillLayout());

      _doubleBufferCanvas = new Canvas(_shell, SWT.NO_BACKGROUND);
      _doubleBufferCanvas.addPaintListener(this);

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
      _world = new World3D(TEXTURE);
      _world.add(new Die(sides, DIE_MAP.get(sides).clone(), scale, results, baseRgb, null));
      _world.add(new Die(6, DIE_MAP.get(6).clone(), scale, 3, baseRgb, null));

      _shell.setSize(100, 100);
      _shell.open();

      // =============================================================
      // Register a listener for the Close event on the die Shell.
      // This disposes the Die Shell
      // =============================================================
      _shell.addListener(SWT.Close, event -> {
         System.out.println("Die Shell handling Close event, about to dispose this Shell");
         _shell.dispose();
      });

      start();
   }

   @Override
   public void run() {
      while (!_shell.isDisposed()) {
         try {
            Thread.sleep(16);

         } catch (InterruptedException e) {
            break;
         }

         if (_shell.isDisposed()) {
            break;
         }
         _world.update();

         _shell.getDisplay().asyncExec(() -> {
            if (_region != null) {
               _region.dispose();
            }
            _region = _world.getOutline();

            if (!_shell.isDisposed()) {
               _image = paintImage(_shell.getDisplay());
               if ((_region != null) && (!_region.isDisposed())) {
                  _shell.setRegion(_region);
                  Rectangle size = _region.getBounds();
                  // clip the height & width to [1, 100]
                  size.height = Math.min(Math.max(size.height, 1), 400);
                  size.width = Math.min(Math.max(size.width, 1), 400);
                  //// add 25 to each dimension to prevent clipping
                  size.width += 25;
                  size.height += 25;
                  _shell.setSize(size.width + size.x, size.height + size.y);
               }
            }
         });
      }
   }


   @Override
   public void paintControl(PaintEvent event) {
      // Draws the buffer image onto the canvas.
      if ((_image != null) && (!_image.isDisposed())) {
         event.gc.drawImage(_image, 0, 0);
      }

      if (_imageGC != null) {
         _imageGC.dispose();
         _imageGC = null;
      }
   }

   Image paintImage(Display display) {
      synchronized (_world) {

         // Creates new image only when absolutely necessary.
         Point canvasSize = _doubleBufferCanvas.getSize();
         int width = canvasSize.x;
         int height = canvasSize.y;
         if ((width > 0) && (height > 0)) {
//          Image image = (Image) _doubleBufferCanvas.getData("double-buffer-image");
//            if (   (_image == null)
//                || (_image.getBounds().width  < canvasSize.x)
//                || (_image.getBounds().height < canvasSize.y))
            {
               if (_image != null) {
                  width  = Math.max(width,  _image.getBounds().width);
                  height = Math.max(height, _image.getBounds().height);
                  _image.dispose();
               }
//               // Add 10 to the image size to reduce the chance it needs to be recreated
//               width += 10; height += 10;

               System.out.println("creating image (" + width + ", " + height + ")");
               _image = new Image(display, width, height);
//               _doubleBufferCanvas.setData("double-buffer-image", image);
            }
         }

         // Initializes the graphics context of the image.
         try {
            _imageGC = new GC(_image);
            _world.paintControl(display, _imageGC);
         }
         catch (IllegalArgumentException e) {
            System.out.println(e);
         }
         return _image;
      }
   }
}