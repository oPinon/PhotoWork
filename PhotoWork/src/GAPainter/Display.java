package GAPainter;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;


public class Display extends JPanel implements ActionListener{

	private BufferedImage source, img;
	Timer timer;
	
	public Display(BufferedImage source) {
		this.source = source;
		img = new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_RGB);
		
		JFrame frame = new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(2*source.getWidth(), source.getHeight()+30);
        frame.setLocationRelativeTo(null);
        frame.setTitle("VectorGeneration");
        frame.setResizable(false);
		frame.setVisible(true);
		
		setFocusable(true);
        setDoubleBuffered(true);
        timer = new Timer(16, this);
        timer.start();
	}
	
	public void setImage(BufferedImage img) {
		this.img = img;
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(source,0,0,this);
		g.drawImage(img,source.getWidth(),0,this);
		Toolkit.getDefaultToolkit().sync();
        g.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		repaint();
	}
}
