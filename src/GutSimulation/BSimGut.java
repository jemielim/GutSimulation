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
import bsim.BSimChemicalField;

/**
 * An example simulation definition to illustrate the key features of a BSim model.</br>
 */
public class BSimGut {

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
		double totpop = 100;
		double specfrac = 0.5; //Fraction of species that is species 1.
		
		
		/*********************************************************
		 * Set up the environment for the simulation
		 */
		BSim sim = new BSim();			// New simulation object
		sim.setDt(1);				// Global dt (time step)
		sim.setSimulationTime(1000);		// Total simulation time [sec]
		sim.setTimeFormat("0.00");		// Time format (for display etc.)
		double xr = 400;
		double yr = 100;
		double zr = 100;
		sim.setBound(xr, yr, zr);		// Simulation boundaries [um]
		sim.setSolid(true, true, true);
		
		

		
		/*********************************************************
		 * Set up the chemical field. The code will be set up to have 5 possible chemical fields (A, B, C, D) that respectively are food sources for
		 * species one and two (A and B), chemoattractants for species one and two to itself (C and D) and a chemorepellent that species one produces
		 *  to act on species two. Kind of a lot, but we can narrow things down latter as necessary.
		 * We'll assume all cytokines and nutrients have the same diffusivity and decay rate.
		 */
		
		
		
		final double c = 0; // molecules
		final double decayRate = 10e2;
		final double diffusivity = 10e2; // (microns)^2/sec
		final BSimChemicalField field = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
			
		final BSimChemicalField fieldA = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		final BSimChemicalField fieldB = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		final BSimChemicalField fieldC = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		final BSimChemicalField fieldD = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		final BSimChemicalField fieldE = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		
		//Need two integrated fields for the bacterium to respond to
		final BSimChemicalField fieldOne = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		final BSimChemicalField fieldTwo = new BSimChemicalField(sim, new int[]{10,10,10}, diffusivity, decayRate);
		
		
		

		final double chemAProd = 1e9;
		final double chemBProd = 1e9;
		final double chemCProd = 1e9;
		final double chemDProd = 1e9;
		final double chemEProd = 1e9;
		
		/*********************************************************
		 * 
		 */
		
		/*********************************************************
		 * Step 2: Extend BSimParticle as required and create vectors marked final
		 * As an example let's make a bacteria that turns red upon colliding
		 */				
		class BSimMultiSpecies extends BSimBacterium {
			// local field for setting whether a collision is occurring
			@SuppressWarnings("unused")
			private boolean collision = false;		
			public int species = 0;
			
			// Constructor for the BSimTutorialBacterium
			public BSimMultiSpecies(BSim sim, Vector3d position) {
				super(sim, position); // default radius is 1 micron	
				setSpecies(0);
			}
			
			public int getSpecies() {
				return species;
			}
			public void setSpecies(int species) {
				this.species = species;
			}
			@SuppressWarnings("unchecked")
			@Override
			public void replicate() {
				setRadiusFromSurfaceArea(surfaceArea(replicationRadius)/2);
				BSimMultiSpecies child = new BSimMultiSpecies(sim, new Vector3d(position));	
				child.setRadius(radius);
				child.setSurfaceAreaGrowthRate(surfaceAreaGrowthRate);
				child.setChildList(childList);
				childList.add(child);
			}
			
			@Override
//			Bacteria move etc. and also add chemical to the global field.
			public void action() {
				super.action();
				//if (Math.random() < sim.getDt())
					
					//Produce chemoattractants for the specific species
					if(species ==0){
					fieldC.addQuantity(position, chemCProd);
					fieldE.addQuantity(position, chemEProd);
					
					}
					else {
					fieldD.addQuantity(position, chemDProd);
					}
				field.addQuantity(position, chemDProd);
			}
		
 	
		}

		
		@SuppressWarnings("hiding")
		class BSimChemicalFieldGut extends BSimChemicalField{


			
			
			public BSimChemicalFieldGut(BSim sim, int[] boxes,
					double diffusivity, double decayRate) {
				super(sim, boxes, diffusivity, decayRate);
				// TODO Auto-generated constructor stub
			}

			public void addFields(BSimChemicalField chemA, BSimChemicalField chemB)
			{
				//Combine together the fields from two different chemical fields. 
				//Most naive way to combine together chemoattractants and chemorepellents
				//this.setConc(x, y, z, chemEProd);
				
			
			}
			
		}
		
		
		// Set up a list of bacteria that will be present in the simulation
		
		//Species
		final Vector<BSimMultiSpecies> bacOne = new Vector<BSimMultiSpecies>();
		final Vector<BSimMultiSpecies> bacTwo = new Vector<BSimMultiSpecies>();
		
		final Vector<BSimMultiSpecies> childOne = new Vector<BSimMultiSpecies>();
		final Vector<BSimMultiSpecies> childTwo = new Vector<BSimMultiSpecies>();
		
		//Semi-clumsy way to make two species, but it makes dealing with childList easier.
		while(bacOne.size() <= specfrac*totpop) {		
			// Creates a new bacterium with random position within the boundaries
			
			BSimMultiSpecies b = new BSimMultiSpecies(sim, 
					new Vector3d(Math.random()*sim.getBound().x, 
								Math.random()*sim.getBound().y, 
								Math.random()*sim.getBound().z));
			// If the bacterium doesn't intersect any others then add it to the overall list
			if(!b.intersection(bacOne)) bacOne.add(b);	
			b.setRadius();
			b.setSurfaceAreaGrowthRate(growthRateOne);
			
			b.setChildList(childOne);
			b.setGoal(field);
			bacOne.add(b);
		}
		
		while(bacTwo.size() <= (1-specfrac)*totpop) {		
			// Creates a new bacterium with random position within the boundaries
			
			BSimMultiSpecies b = new BSimMultiSpecies(sim, 
					new Vector3d(Math.random()*sim.getBound().x, 
								Math.random()*sim.getBound().y, 
								Math.random()*sim.getBound().z));
			// If the bacterium doesn't intersect any others then add it to the overall list
			if(!b.intersection(bacOne)) bacTwo.add(b);	
			b.setRadius();
			b.setSurfaceAreaGrowthRate(growthRateTwo);
			
			b.setChildList(childTwo);
			b.setGoal(fieldA);

			bacTwo.add(b);
		}
		
		
		//Extend BSimP3DDrawer to change the perspective on the simulation region
		
		class BSimP3DDrawerGut extends BSimP3DDrawer {
			private boolean cameraIsInitialised = false;
			
			public BSimP3DDrawerGut(BSim sim, int width, int height) {
				super(sim, width, height);
				// TODO Auto-generated constructor stub
			}

			@Override
			public void scene(PGraphics3D arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void draw(Graphics2D g) {
				p3d.beginDraw();

				if(!cameraIsInitialised){
					p3d.camera(-(float)bound.x*0f, (float)bound.y*2.5f, (float)bound.z*2f, 
						(float)bound.x*0.5f, (float)bound.y*0f, (float)bound.z*0.f, 
						0, 0,1);
					cameraIsInitialised = true;
				}
				
				p3d.textFont(font);
				p3d.textMode(PConstants.SCREEN);

				p3d.sphereDetail(10);
				p3d.noStroke();		
				p3d.background(0, 0, 0);	

				scene(p3d);
				boundaries();
				time();

				p3d.endDraw();
				g.drawImage(p3d.image, 0,0, null);
			}
			
		
		}
		

		/*********************************************************
		 * Step 3: Implement tick() on a BSimTicker and add the ticker to the simulation	  
		 */
		sim.setTicker(new BSimTicker() {
			@Override
			public void tick() {
				for(BSimMultiSpecies b : bacOne) {
					b.action();		
					//b.updatePosition();					
				}
				
				bacOne.addAll(childOne);
				childOne.clear();
				
				for(BSimMultiSpecies b : bacTwo){
					b.action();		
				//	b.updatePosition();					
				}
				bacTwo.addAll(childTwo);
				childTwo.clear();
			
				//field.addQuantity(new Vector3d(10,10,10),1e6);		
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
		BSimP3DDrawer drawer = new BSimP3DDrawerGut(sim, 1200,800) {
			
			@Override
			public void scene(PGraphics3D p3d) {	
				// loop through all the bacteria and draw them (red if colliding, green if not)
				for(BSimMultiSpecies b : bacOne) {
				
					draw(b, (b.getSpecies() == 0) ? Color.RED: Color.BLUE);
				}	
				for(BSimMultiSpecies b : bacTwo) {
					draw(b, Color.BLUE);
					//draw(b, (b.getSpecies() == 0) ? Color.RED: Color.BLUE);
				}
				draw(field, Color.BLUE, (float)(255/c));						
			
				
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

		/* 
		 * BSimMovExporter is a concrete BSimExporter for creating Quicktime movies
		 * Available setters:
		 * 	BSimMovExporter#setSpeed()
		 */			
		BSimMovExporter movExporter = new BSimMovExporter(sim, drawer, resultsDir + "BSim.mov");
		movExporter.setDt(0.03);
		sim.addExporter(movExporter);			

		/* 
		 * BSimPngExporter is another concrete BSimExporter for creating png images. 
		 */
		BSimPngExporter pngExporter = new BSimPngExporter(sim, drawer, resultsDir);
		pngExporter.setDt(0.5);
		sim.addExporter(pngExporter);			

		/* 
		 * BSimLogger is an abstract BSimExporter that requires an implementation of during().
		 * It provides the convenience method write() 
		 */
		BSimLogger logger = new BSimLogger(sim, resultsDir + "tutorialExample.csv") {
			
			@Override
			public void before() {
				super.before();
				// Write a header containing the names of variables we will be exporting
				write("time,collisions"); 
			}
			
			@Override
			public void during() {
				
			}
		};
		sim.addExporter(logger);

		/*
		 * Add your own exporters by extending BSimExporter like
		 * 
		 * BSimExporter e = new BSimExporter(){}; 
		 *
		 */		

		/*********************************************************
		 * Step 6: Call sim.preview() to preview the scene or sim.export() to set exporters working 
		 */
		sim.preview();

	}
}
