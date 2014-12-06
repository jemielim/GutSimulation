package GutSimulation;

import java.awt.Color;
import java.util.Vector;

import javax.vecmath.Vector3d;

import processing.core.PGraphics3D;
import processing.core.PConstants;

import java.awt.Graphics2D;

import bsim.BSim;
import bsim.BSimTicker;
import bsim.BSimUtils;
import bsim.draw.BSimP3DDrawer;
import bsim.export.BSimLogger;
import bsim.export.BSimMovExporter;
import bsim.export.BSimPngExporter;
import bsim.particle.BSimBacterium;
import bsim.particle.BSimParticle;
import bsim.BSimChemicalField;

public class BSimGutReadOut {

	public static <BSimChemicalFieldGut> void main(String[] args) {

		/*********************************************************
		 * Step 1: Create a new simulation object and set environmental properties
		 * Available setters:
		 * 	BSim#setDt() defaults to 0.01
		 * 	BSim#setSimulatonTime() 
		 * 	BSim#setTimeFormat() defaults to "0.00"
		 * 	BSim#setBound() defaults to (100,100,100)
		 * 	BSim#setSolid() defaults to {false,false,false} and the particles wrap
		 * 	BSim#setVisc() defaults to 2.7e-3 Pa s
		 * 	BSim#setTemperature() defaults to 305 K 
		 */
		
		//Relevant biological parameters for bacteria
				double growthRateOne = 0;
				double growthRateTwo = 0;
				
				// Add 10 bacteria to the simulation
				final int totpop = 100;
				double specfrac = 0.5; //Fraction of species that is species 1.
		
		BSim sim = new BSim();			// New simulation object
		sim.setDt(0.01);				// Global dt (time step)
		sim.setSimulationTime(10);		// Total simulation time [sec]
		sim.setTimeFormat("0.00");		// Time format (for display etc.)
		final double xr = 400;
		final double yr = 100;
		final double zr = 100;
		sim.setBound(xr, yr, zr);		// Simulation boundaries [um]
		sim.setSolid(true, true, true);
		
		/*********************************************************
		 * Set up the chemical field. 5 fields is overkill, but this way we'll have them.
		 */
		
		//Will want to change this to use 2 parameters: relative repeller/atractor, wall food produced
		
		final double c = 12e5; // molecules
		final double decayRate = 0.9;
		final double diffusivity = 890; // (microns)^2/sec
		final BSimChemicalField field = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);

		/*********************************************************
		 * Step 2: This is taken from tutorial. Will want to incorporate multispecies code here
		 */				
		class BSimGutReadOutBacterium extends BSimBacterium {
			// local field for setting whether a collision is occurring
			private boolean collision = false;	

			// Constructor for the BSimGutReadOutBacterium
			public BSimGutReadOutBacterium(BSim sim, Vector3d position) {
				super(sim, position); // default radius is 1 micron			
			}

			// What happens in an interaction with another bacterium?
			public void interaction(BSimGutReadOutBacterium p) {
				// If the bacteria intersect, then set the collision state to 'true'
				if(outerDistance(p) < 0) {
					collision = true;
					p.collision = true;
				}
			}
		}	
		
		// Set up a list of bacteria that will be present in the simulation
		final Vector<BSimBacterium> bacteria = new Vector<BSimBacterium>();		
		while(bacteria.size() < 30) {	
			BSimBacterium p = new BSimBacterium(sim, 
					new Vector3d(Math.random()*sim.getBound().x, 
							Math.random()*sim.getBound().y, 
							Math.random()*sim.getBound().z)) {
				// Bacteria move etc. and also add chemical to the global field.
				public void action() {
					super.action();
					if (Math.random() < sim.getDt())
						field.addQuantity(position, 1e9);					
				}
			};
			// Chemotaxis according to chemical field strength
			p.setGoal(field);
			
			if(!p.intersection(bacteria)) bacteria.add(p);		
		}

		/*********************************************************
		 * Step 3: Implement tick() on a BSimTicker and add the ticker to the simulation	  
		 */
		sim.setTicker(new BSimTicker() {
			@Override
			public void tick() {
				for(BSimBacterium b : bacteria) {
					b.action();		
					b.updatePosition();
				}
				field.update(); 
			}		
		});

		/*********************************************************
		 * Step 4: Implement draw(Graphics) on a BSimDrawer and add the drawer to the simulation 
		 * 
		 * Here we use the BSimP3DDrawer which has already implemented draw(Graphics) to draw boundaries
		 * and a clock but still requires the implementation of scene(PGraphics3D) to draw particles
		 * You can use the draw(BSimParticle, Color) method to draw particles 
		 */
		BSimP3DDrawer drawer = new BSimP3DDrawer(sim, 800,600) {
			
			@Override
			public void scene(PGraphics3D p3d) {	
				// loop through all the bacteria and draw them (red if colliding, green if not)
				for(BSimBacterium b : bacteria) {
					draw(b, Color.RED );
				}			
			}
		};
		sim.setDrawer(drawer);			// add the drawer to the simulation object.		

		/*********************************************************
		 * Step 5: Implement before(), during() and after() on BSimExporters and add them to the simulation
		 * Available setters:
		 * 	BSimExporter#setDt()
		 */		

		// Create a new directory for the simulation results
				String resultsDir = BSimUtils.generateDirectoryPath("./results/");			
				
				BSimLogger trackerXYZ = new BSimLogger(sim, resultsDir + "trackerXYZ.csv") {
					@Override
					public void during() {
						for(int i = 1; i < bacteria.size(); i++) {
							write(sim.getFormattedTime()+","+bacteria.get(i).getPosition().x+","+bacteria.get(i).getPosition().y+","+bacteria.get(i).getPosition().z+","+field.getConc(1,1,1));
						}
					}
				};
				trackerXYZ.setDt(0.1);
				sim.addExporter(trackerXYZ);

				// run the simulation
				sim.export();
		

		/*
		 * Add your own exporters by extending BSimExporter like
		 * 
		 * BSimExporter e = new BSimExporter(){}; 
		 *
		 */		

		/*********************************************************
		 * Step 6: Call sim.preview() to preview the scene or sim.export() to set exporters working 
		 */
		sim.export();

	}	
	
}
