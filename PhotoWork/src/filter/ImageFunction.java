package filter;

/**
 * Enumeration des différentes fonctions de l'application. Indique aussi si elles peuvent s'appliquer a plusieurs images
 * en meme temps.
 * 
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

	public static ImageFunction fromName(String name){
		switch(name){
		case "AUTO_BALANCE": 	  case "Auto Balance": 		 return AUTO_BALANCE;
		case "BLUR":              case "Blur":		   	 	 return BLUR;
		case "HDR_EQUALIZER": 	  case "HDR Equalizer": 	 return HDR_EQUALIZER;
		case "FOURIER_TRANSFORM": case "Fourier Transform":  return FOURIER_TRANSFORM;
		case "SCAN":  		 	  case "Scan":				 return SCAN;
		case "GA_PAINTER":	 	  case "GA Painter":   		 return GA_PAINTER;
		default: 								   			 return null;
		}
	}
}
