package edu.smith.cs.csc212.fishgrid;

/**
 * This class extends Rock to create a rock which moves down till it hits something.
 * @author Per Van Dyke
 *
 */

public class FallingRock extends Rock {

	private int worldHeight;
	private int worldWidth;
	public FallingRock(World world) {
		super(world);
		worldHeight = world.getHeight();
		worldWidth = world.getWidth();
	}
	
	@Override
	public void step() {
		boolean justMoved = false; //this is so the rock will spend a step at the top of the screen
		//if rock is at the bottom of the screen, move it to the top
		if (this.getY() >= worldHeight-1) {
			this.setPosition(rand.nextInt(worldWidth), 0);
			justMoved = true;
		}
		if (justMoved == false) {
			//move the rock down
			this.moveDown();
		} else {
			justMoved = false;
		}
	}
	
}
