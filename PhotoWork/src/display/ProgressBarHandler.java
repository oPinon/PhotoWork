package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;

public class ProgressBarHandler extends Thread{
	ProgressBar progressBar;
	int selection;

	public ProgressBarHandler(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public void run(){ //temporary

		while(true){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	


			if (progressBar.isDisposed()) return;
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(progressBar.isDisposed()) return;
					selection++;
					if(selection>progressBar.getMaximum()) selection= 0;
					progressBar.setSelection(selection);
				}
			});
		}
	}
}

//new Thread(new Runnable() {
//public void run() {
//	while (true) {
//		try { Thread.sleep(100); } catch (Exception e) { }
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				int selection=progressBar.getSelection();
//				selection++;
//				if(selection>100) selection= 0;
//				progressBar.setSelection(selection);
//			}
//		});
//	}
//}
//}).start();
