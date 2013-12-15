package gAPainter;
import java.awt.Graphics;


public interface Shape {

	public void mutate();
	public void paint(Graphics g, int scale);
}
