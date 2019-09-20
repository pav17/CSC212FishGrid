package edu.smith.cs.csc212.fishgrid;

public class FallingRock extends Rock {

	public FallingRock(World world) {
		super(world);
		// TODO Auto-generated constructor stub
	}
	
	public void step() {
		this.moveDown();
	}
	
}
