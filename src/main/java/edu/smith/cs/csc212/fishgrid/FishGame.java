package edu.smith.cs.csc212.fishgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class manages our model of gameplay: missing and found fish, etc.
 * @author jfoley
 * modified by @author Per Van Dyke
 */
public class FishGame {
	/**
	 * This is the world in which the fish are missing. (It's mostly a List!).
	 */
	World world;
	/**
	 * The player (a Fish.COLORS[0]-colored fish) goes seeking their friends.
	 */
	P2Fish player;
	/**
	 * The home location.
	 */
	FishHome home;
	
	Random rand;
	
	/**
	 * These are the missing fish!
	 */
	List<P2Fish> missing;
	
	/**
	 * These are fish we've found!
	 */
	List<P2Fish> found;
	
	/**
	 * These are the fish returned home
	 */
	List<P2Fish> returned;
	
	/**
	 * Number of steps!
	 */
	int stepsTaken;
	
	/**
	 * Score!
	 */
	int score;
	
	/**
	 * Number of fish in the game excluding the player
	 */
	int fishPresent;
	
	/**
	 * Number of Rocks
	 */
	public static final int numberOfRocks = 8;
	
	/**
	 * Number of falling rocks
	 */
	public static final int numberOfFallingRocks = 3;
	
	/**
	 * Create a FishGame of a particular size.
	 * @param w how wide is the grid?
	 * @param h how tall is the grid?
	 */
	public FishGame(int w, int h) {
		world = new World(w, h);
		
		rand = new Random();
		
		missing = new ArrayList<P2Fish>();
		found = new ArrayList<P2Fish>();
		returned = new ArrayList<P2Fish>();
		
		// Add a home!
		home = world.insertFishHome();
		
		for (int i=0; i<numberOfRocks; i++) {
			world.insertRockRandomly();
		}
		
		for (int i=0; i<numberOfFallingRocks; i++) {
			world.insertFallingRockRandomly();
		}
		
		world.insertSnailRandomly();
		
		world.insertFishFoodRandomly();
		
		// Make the player out of the 0th fish color.
		player = new P2Fish(0, world);
		// Start the player at "home".
		player.setPosition(home.getX(), home.getY());
		player.markAsPlayer();
		world.register(player);
		
		// Generate fish of all the colors but the first into the "missing" List.
		for (int ft = 1; ft < P2Fish.COLORS.length; ft++) {
			P2Fish friend = world.insertFishRandomly(ft);
			missing.add(friend);
		}
		this.fishPresent = missingFishLeft();
	}
	
	
	/**
	 * How we tell if the game is over: if missingFishLeft() == 0.
	 * @return the size of the missing list.
	 */
	public int missingFishLeft() {
		return missing.size();
	}
	
	/**
	 * This method is how the Main app tells whether we're done.
	 * @return true if the player has won (or maybe lost?).
	 */
	public boolean gameOver() {
		if (returned.size() == this.fishPresent) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Update positions of everything (the user has just pressed a button).
	 */
	public void step() {
		// Keep track of how long the game has run.
		this.stepsTaken += 1;
		
		for (P2Fish f : found) {
			//if fish needs to increment found timer, do
			if (found.indexOf(f) != 0) {
				f.foundTimer++;
			}
			//if found timer is greater than 15, start seeing if it runs away
			if (f.foundTimer > 15) {
				float lostChance = rand.nextFloat();
				//if fish runs away, add it back to missing, and re-set the found timer
				if (lostChance <= .10f) {
					missing.add(f);
					f.foundTimer = 0;
				}
			}
		}
		//make sure any fish that have run away are no longer found
		for (P2Fish f : missing ) {
			if (found.contains(f)) {
				found.remove(f);
			}
		}
		
		// These are all the objects in the world in the same cell as the player.
		List<WorldObject> overlap = this.player.findSameCell();
		// The player is there, too, let's skip them.
		overlap.remove(this.player);
		
		// If we find a fish, remove it from missing.
		for (WorldObject wo : overlap) {
			// It is missing if it's in our missing list.
			if (missing.contains(wo)) {
				// Remove this fish from the missing list.
				missing.remove(wo);
				// In here I'm sure it's a fish.
				P2Fish f = (P2Fish) wo;
				// Increase score when you find a fish!
				score += f.scoreValue;
				// Add to found.
				this.found.add(f);
			} else if (wo.isHome()) {
				int foundLength = this.found.size();
				for (int i = 0; i < foundLength; i++) {
					this.returned.add(this.found.get(0));
					world.remove(this.found.get(0));
					this.found.remove(0);
				}
			} else if (wo.isFood()) {
				world.remove(wo);
				score += 10;
			}
		}
		
		// Make sure missing fish *do* something.
		wanderMissingFish();
		//check each missing fish to see if it's found something
		for (P2Fish f : missing) {
			List<WorldObject> fishOverlap = f.findSameCell();
			fishOverlap.remove(f);
			for (WorldObject wo : fishOverlap) {
				//if it's found home, become returned
				if (wo.isHome()) {
					this.returned.add(f);
					world.remove(f);
				//if it's found food, eat it
				} else if (wo.isFood()) {
					world.remove(wo);
				}
			}
		}
		//make sure any returned fish are removed from missing
		for (P2Fish f : returned) {
			if (missing.contains(f)) {
				missing.remove(f);
			}
		}
		
		//see if we will create more FishFood
		float foodChance = rand.nextFloat();
		if (foodChance <= .05f) {
			world.insertFishFoodRandomly();
		}
		
		// When fish get added to "found" they will follow the player around.
		World.objectsFollow(player, found);
		// Step any world-objects that run themselves.
		world.stepAll();
	}
	
	/**
	 * Call moveRandomly() on all of the missing fish to make them seem alive.
	 */
	private void wanderMissingFish() {
		Random rand = ThreadLocalRandom.current();
		for (P2Fish lost : missing) {
			if (lost.fastScared == false) {
				// 30% of the time, lost fish move randomly.
				if (rand.nextDouble() < 0.3) {
					lost.moveRandomly();
				}
			} else {
				// 80% of the time, lost fish move randomly.
				if (rand.nextDouble() < 0.8) {
					lost.moveRandomly();
				}
			}
			
		}
	}

	/**
	 * This gets a click on the grid. We want it to destroy rocks that ruin the game.
	 * @param x - the x-tile.
	 * @param y - the y-tile.
	 */
	public void click(int x, int y) {
		// TODO(FishGrid) use this print to debug your World.canSwim changes!
		System.out.println("Clicked on: "+x+","+y+ " world.canSwim(player,...)="+world.canSwim(player, x, y));
		List<WorldObject> atPoint = world.find(x, y);
		// if there is something at point and it is a rock, delete it
		if (atPoint.size() > 0 && atPoint.get(0).isRock()) {
			world.remove(atPoint.get(0));;
		}

	}
	
}
