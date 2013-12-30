package network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Pool {

  private BlockingQueue<Task> tasks;
  private List<PoolThread> threads;

  public Pool(int noOfThreads, int maxNoOfTasks) {
    
    this.tasks = new ArrayBlockingQueue<Task>(maxNoOfTasks);
    this.threads = new ArrayList<PoolThread>();

    for(int i = 0; i < noOfThreads; ++i) {
      threads.add(new PoolThread(tasks));
    }
    
    for(PoolThread thread : threads){
      thread.start();
    }
  }

  public synchronized void addTask(Task task) throws InterruptedException {
    this.tasks.put(task);
  }

  public synchronized void stop() {
    for(PoolThread thread : threads) {
      thread.interrupt();
    }
  }

}