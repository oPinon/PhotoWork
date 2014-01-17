package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Explorateur de fichier, permettant d'en charger et d'en sauvegarder plusieurs � la fois.
 *
 */
public class FileBrowser {
	Shell shell;
	String[] chosenFiles;
	int extension;

	public FileBrowser(Display display, int action) {
		shell=  new Shell(display);
		if(action==SWT.OPEN){
			load();
		}
		if(action==SWT.SAVE){
			save();
		}

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}
	private void load() {

		FileDialog dialog = new FileDialog(shell, SWT.MULTI);
		dialog.setFilterNames(new String[] { "All Files (*.*)", "PNG", "JPG", "GIF" });
		dialog.setFilterExtensions(new String[] { "*.*", "*.png", "*.jpg", "*.gif" }); 

		String path = dialog.open();
		if (path != null) {
			chosenFiles = dialog.getFileNames(); 
			String[] filenames = dialog.getFileNames();
			String filterPath = dialog.getFilterPath();

			chosenFiles = new String[filenames.length];

			for(int i = 0; i < filenames.length; i++)
			{
				if(filterPath != null && filterPath.trim().length() > 0) {
					chosenFiles[i] = filterPath+"/"+filenames[i];
				}
				else  {
					chosenFiles[i] = filenames[i];
				}
			}


		}
		shell.dispose();
	}

	private void save() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "PNG", "JPG", "GIF" });
		dialog.setFilterExtensions(new String[] { "*.png", "*.jpg", "*.gif" }); 

		dialog.setFileName("result");

		String path = dialog.open();
		chosenFiles = new String[]{path};

		switch(dialog.getFilterIndex()){
		case 0: extension= SWT.IMAGE_PNG ;break;
		case 1: extension= SWT.IMAGE_GIF ;break;
		case 2: extension= SWT.IMAGE_JPEG ;break;
		default: extension= 0 ;break;
		}

		shell.dispose();
	}
} 