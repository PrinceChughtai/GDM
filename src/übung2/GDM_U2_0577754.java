package �bung2;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
     Opens an image window and adds a panel below the image
*/
public class GDM_U2_0577754 implements PlugIn {

    ImagePlus imp; // ImagePlus object
	private int[] origPixels;
	private int width;
	private int height;
	
	
    public static void main(String args[]) {
		//new ImageJ();
    	//IJ.open("/users/barthel/applications/ImageJ/_images/orchid.jpg");
    	IJ.open("C:\\Users\\alina\\Downloads\\orchid.jpg");
		
		GDM_U2_0577754 pw = new GDM_U2_0577754();
		pw.imp = IJ.getImage();
		pw.run("");
	}
    
    public void run(String arg) {
    	if (imp==null) 
    		imp = WindowManager.getCurrentImage();
        if (imp==null) {
            return;
        }
        CustomCanvas cc = new CustomCanvas(imp);
        
        storePixelValues(imp.getProcessor());
        
        new CustomWindow(imp, cc);
    }


    private void storePixelValues(ImageProcessor ip) {
    	width = ip.getWidth();
		height = ip.getHeight();
		
		origPixels = ((int []) ip.getPixels()).clone();
	}


	class CustomCanvas extends ImageCanvas {
    
        CustomCanvas(ImagePlus imp) {
            super(imp);
        }
    
    } // CustomCanvas inner class
    
    
    class CustomWindow extends ImageWindow implements ChangeListener {
         
        private JSlider jSliderBrightness;
		private JSlider jSliderSaturation;
		private JSlider jSliderContrast;
		private JSlider jSliderRotation;
		private double saturation;
		private double brightness;
		private double contrast;
		private double rotation;
		

		CustomWindow(ImagePlus imp, ImageCanvas ic) {
            super(imp, ic);
            addPanel();
        }
    
        void addPanel() {
        	//JPanel panel = new JPanel();
        	Panel panel = new Panel();
        	
        	contrast   = 1;
			saturation = 1;
			rotation   = 0;

            panel.setLayout(new GridLayout(4, 1));
            jSliderBrightness = makeTitledSilder("Helligkeit", 0, 200, 100);
            jSliderSaturation = makeTitledSilder("Saetigung", 0, 8, 4);
            jSliderContrast = makeTitledSilder("Kontrast", 0, 10, 5);
            jSliderRotation = makeTitledSilder("Rotation", 0, 360, 0);
            panel.add(jSliderBrightness);
            panel.add(jSliderSaturation);
            panel.add(jSliderContrast);
            panel.add(jSliderRotation);
            
            add(panel);
            
            pack();
         }
      
        private JSlider makeTitledSilder(String string, int minVal, int maxVal, int val) {
		
        	JSlider slider = new JSlider(JSlider.HORIZONTAL, minVal, maxVal, val );
        	Dimension preferredSize = new Dimension(width, 50);
        	slider.setPreferredSize(preferredSize);
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(), 
					string, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
			slider.setMajorTickSpacing((maxVal - minVal)/10 );
			slider.setPaintTicks(true);
			slider.addChangeListener(this);
			
			return slider;
		}
        
        private void setSliderTitle(JSlider slider, String str) {
			TitledBorder tb = new TitledBorder(BorderFactory.createEtchedBorder(),
				str, TitledBorder.LEFT, TitledBorder.ABOVE_BOTTOM,
					new Font("Sans", Font.PLAIN, 11));
			slider.setBorder(tb);
		}

		public void stateChanged( ChangeEvent e ){
			JSlider slider = (JSlider)e.getSource();

			if (slider == jSliderBrightness) {
				brightness = slider.getValue()-100;
				String str = "Helligkeit " + brightness; 
				setSliderTitle(jSliderBrightness, str); 
			}
			
			if (slider == jSliderSaturation) {
				saturation = slider.getValue();
				saturation = saturation < 5 ? saturation / 4 : saturation - 3 ;
				String str = "Seatigung " + saturation; 
				setSliderTitle(jSliderSaturation, str); 
			}
			
			if (slider == jSliderContrast) {
				contrast = slider.getValue();
				contrast = contrast < 6 ? contrast / 5 : (contrast < 7 ? 2 : contrast * 2 - 10) ;
				String str = "Kontrast " + contrast; 
				setSliderTitle(jSliderContrast, str); 
			}
			
			if (slider == jSliderRotation) {
				rotation = slider.getValue();				
				String str = "Rotation " + rotation;
				rotation = Math.toRadians(rotation);
				setSliderTitle(jSliderRotation, str); 
			}
			
			changePixelValues(imp.getProcessor());
			
			imp.updateAndDraw();
		}

		
		private void changePixelValues(ImageProcessor ip) {
			
			
			// Array fuer den Zugriff auf die Pixelwerte
			int[] pixels = (int[])ip.getPixels();
			
			for (int y=0; y<height; y++) {
				for (int x=0; x<width; x++) {
					int pos = y*width + x;
					int argb = origPixels[pos];  // Lesen der Originalwerte 
					
					int r = (argb >> 16) & 0xff;
					int g = (argb >>  8) & 0xff;
					int b =  argb        & 0xff;
					
					
					// anstelle dieser drei Zeilen später hier die Farbtransformation durchführen,
					// die Y Cb Cr -Werte verändern und dann wieder zurücktransformieren
					
					// RGB to YUV Transformation
					int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b); 
					int u = (int) ((b-Y) * 0.493f);
					int v = (int) ((r-Y) * 0.877f);
					
					// Helligkeit
					Y = (int) (Y + brightness);
					
					// Kontrast
					Y = (int) ((Y - 128) * contrast + 128);
					
					// Seatigung
					u = (int) (u * saturation);
					v = (int) (v * saturation);
					
					// Rotation
					u = (int) (Math.cos(rotation) * u - Math.sin(rotation) * u) ;
					v = (int) (Math.sin(rotation) * v + Math.cos(rotation) * v) ;
					
					// YUV to RGB Transforamtion
					int rn = (int) (Y + v / 0.877);
					int gn = (int) (1 / 0.587 * Y - 0.299 / 0.587*r - 0.114 / 0.587 * b);
					int bn = (int) (Y + u / 0.493);
					
					
					// Hier muessen die neuen RGB-Werte wieder auf den Bereich von 0 bis 255 begrenzt werden
					
					 bn = 255 < bn ? 255 : (bn < 0 ? 0 : bn);
					 gn = 255 < gn ? 255 : (gn < 0 ? 0 : gn);
					 rn = 255 < rn ? 255 : (rn < 0 ? 0 : rn);
					
					
					pixels[pos] = (0xFF<<24) | (rn<<16) | (gn<<8) | bn;
				}
			}
		}
		
    } // CustomWindow inner class
} 
