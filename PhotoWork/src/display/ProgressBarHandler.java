package display;

public class ProgressBarHandler {

	private double progress; // in percentage
	private double speed; // in percentage per second
	private long lastChangeTime;
	
	public ProgressBarHandler() {
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
		
		System.out.println((int)getProgress()+"% done ; "+getRemainingTime()+" remaining...");
	}
	
	public double getProgress() { return progress; }
	
	public String getRemainingTime() {
		if(speed!=0) {
			return (int)((100-progress)/speed) + " s";
		}
		return "?? s";
	}
}
