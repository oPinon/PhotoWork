package display;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.SWT;

public class PWProgressBar {
	ProgressBar bar;
	Composite parent;
	String name;

	private double progress; // in percentage
	private double speed; // in percentage per second
	private long lastChangeTime;

	PWProgressBar(final Composite parent, final String name) {
		this.parent = parent;
		this.name = name;
		
		bar= new ProgressBar(parent, SWT.SMOOTH);
		bar.setMinimum(0);
		bar.setMaximum(100);

		bar.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				String string =	name+": "+bar.getSelection() + "% , "+getRemainingTime()+" remaining";
				Point point = bar.getSize();

				FontMetrics fontMetrics = e.gc.getFontMetrics();
				int width = fontMetrics.getAverageCharWidth() * string.length();
				int height = fontMetrics.getHeight();
				e.gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				e.gc.drawString(string, (point.x-width)/2 , (point.y-height)/2, true);
			}
		});

		reset();
	}

	public synchronized void reset() {
		progress = 0;
		speed = 0;
		lastChangeTime = System.currentTimeMillis();
	}

	public synchronized void setProgress(double percentage) {
		long dt = System.currentTimeMillis()-lastChangeTime;
		if(dt!=0) { speed = (percentage-progress)*1000/dt;}
		this.progress = percentage;
		lastChangeTime  = System.currentTimeMillis();
		bar.setSelection((int) progress);
	}

	public double getProgress() {
		return progress; 
	}

	public String getRemainingTime() {
		if(speed>0) {
			return (int)((100-progress)/speed) + " s";
		}
		return "?? s";
	}


}
