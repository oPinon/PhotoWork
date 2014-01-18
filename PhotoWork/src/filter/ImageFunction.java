package filter;

/**
 * Enumeration des differentes fonctions de l'application. Indique aussi si elles peuvent s'appliquer a plusieurs images
 * en meme temps.
 *
 */
public enum ImageFunction {
	AUTO_BALANCE("Auto Balance",true),
	BLUR("Blur",true),
	HDR_EQUALIZER("HDR Equalizer",true),
	FOURIER_TRANSFORM("Fourier Transform",true),
	SCAN("Scan",false),
	GA_PAINTER("GA Painter",false);

	private String name;
	private boolean applicableOnAllFiles;

	ImageFunction(String name, boolean applicableOnAllFiles){
		this.name= name;
		this.applicableOnAllFiles= applicableOnAllFiles;
	}

	public String getName() {
		return name;
	}

	public boolean isApplicableOnAllFiles() {
		return applicableOnAllFiles;
	}
}
