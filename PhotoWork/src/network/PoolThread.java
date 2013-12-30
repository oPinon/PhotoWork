package network;

import java.util.concurrent.BlockingQueue;

public class PoolThread extends Thread {

  private BlockingQueue<Task> tasks;

  public PoolThread(BlockingQueue<Task> tasks) {
    this.tasks = tasks;
  }

  @Override
  public void run(){
    while (true) {
      Task t;
      try {
        t = tasks.take();
        t.execute();
      } catch (InterruptedException e) {
        System.out.println("Interrupted task");
        break;
      }
    }
  }

}
