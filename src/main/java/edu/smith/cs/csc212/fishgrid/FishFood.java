package edu.smith.cs.csc212.fishgrid;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

public class FishFood extends WorldObject {

	public FishFood(World world) {
		super(world);
	}
	
	@Override
	public void draw(Graphics2D g) {
		Shape circle = new Ellipse2D.Double(-0.6, -0.6, 1.2, 1.2);
		Color color = Color.magenta;
		g.setColor(color);
		g.fill(circle);
	}
	
	@Override
	public void step() {
		
	}
}
