/**
 * 
 */
package imageComputing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

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

	// exclusion 
	private Semaphore mutexProd = new Semaphore(1);
	private Semaphore mutexCons = new Semaphore(1);

	private Semaphore nbMess = new Semaphore(0);

	private boolean isClosed;
	// utilise par le client local pour forcer le Timer de rafraichissement du GAPainter a se finir 

	public Buffer()
	{
		tampon = new LinkedBlockingQueue<T>();
	}

	public void put(T message) throws InterruptedException
	{
		if(isClosed) throw new InterruptedException();
		mutexProd.acquire();
		tampon.offer(message);
		mutexProd.release();
		nbMess.release();
	}

	public T take() throws InterruptedException
	{
		nbMess.acquire();
		mutexCons.acquire();
		T message= tampon.poll();
		mutexCons.release();

		return message;
	}

	public void close(){
		isClosed = true;
	}

}
