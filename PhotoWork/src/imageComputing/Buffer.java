/**
 * 
 */
package imageComputing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represente un tampon de Tasks ou de Result entre DisplayUpdater et les Clients, (tasksToDo et tasksDone)
 * en suivant un modele de type producteur/consommateur.
 *
 * Est aussi utilise pour recuperer la progression de traitements en cours, et est dans ce cas la un tampon
 * de Result entre le ComputationThread et le Client (streamBuffer, par analogie aux OutputStreams).
 * 
 * @param <T> soit une tache a accomplir (Task), soit une tache accomplie (Result)
 */
public class Buffer<T> {

	private BlockingQueue<T> tampon;
	//originellement une LinkedList, mais modifiee suite a des problemes de non synchronisation 
	//occasionnels malgre les Semaphores.
	

	// exclusion 
	private Semaphore mutexProd = new Semaphore(1);
	private Semaphore mutexCons = new Semaphore(1);

	private Semaphore nbMess = new Semaphore(0);

	private AtomicBoolean isClosed;
	// utilise pour forcer des Threads de calcul a s'interrompre

	public Buffer(){
		tampon = new LinkedBlockingQueue<T>(); 
		isClosed = new AtomicBoolean(false);
	}

	public void put(T message) throws InterruptedException {
		if(isClosed.get()) throw new InterruptedException();
		
		mutexProd.acquire();
		tampon.add(message);  //ou put sans Semaphores
		mutexProd.release();
		nbMess.release();
	}

	public T take() throws InterruptedException {
		if(isClosed.get()) throw new InterruptedException();
		
		nbMess.acquire();
		mutexCons.acquire();
		T message = tampon.remove(); //ou take sans Semaphores
		mutexCons.release();

		return message;
	}

	public void close(){
		isClosed.set(true);
	}

}
