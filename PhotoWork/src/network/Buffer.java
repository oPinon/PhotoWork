/**
 * 
 */
package network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Repr�sente un tampon de t�ches entre ImageUpdater et les clients, en suivant un mod�le producteur/consommateur.
 * 
 * @author Pierre-Alexandre Durand
 *
 * @param <T> soit une t�che � accomplir (Task), soit une t�che accomplie (Result)
 */
public class Buffer<T> {

	private List<T> tampon;

	// exclusion 
	private Semaphore mutexProd = new Semaphore(1);
	private Semaphore mutexCons = new Semaphore(1);

	private Semaphore nbMess = new Semaphore(0);


	public Buffer()
	{
		tampon = new ArrayList<T>();
	}

	public void put(T message) throws InterruptedException
	{
		mutexProd.acquire();
		tampon.add(message);
		nbMess.release();
		mutexProd.release();
	}

	public T take() throws InterruptedException
	{
		mutexCons.acquire();
		nbMess.acquire();
		T message = tampon.remove(0);
		mutexCons.release();

		return message;
	}


}
