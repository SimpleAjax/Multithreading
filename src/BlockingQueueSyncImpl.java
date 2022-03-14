import java.util.LinkedList;
import java.util.Queue;

public class BlockingQueueSyncImpl<T> {
    final Queue<T> queue;
    int capacity;
    final Object lock = new Object();
    int size;
    public BlockingQueueSyncImpl(int capacity) {
        queue = new LinkedList<>();
        this.capacity = capacity;
        size = 0;
    }

    public void enqueue(T val) throws InterruptedException {
        synchronized (lock) {
            while (size == capacity) {
                lock.wait();
            }
            queue.add(val);
            size++;
            if (size == 1) lock.notifyAll();
        }
    }

    public T dequeue() throws InterruptedException {
        synchronized (lock) {
            while (size == 0) {
                lock.wait();
            }
            size--;
            if (size == capacity - 1) lock.notifyAll();
            return queue.remove();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        BlockingQueueSyncImpl<Integer> bq = new BlockingQueueSyncImpl<>(3);
        Thread producer1 = new Thread(new Producer(bq));
        Thread consumer1 = new Thread(new Consumer(bq));
        Thread consumer2 = new Thread(new Consumer(bq));

        producer1.start();
        consumer1.start();
        consumer2.start();

//        producer1.join();
//        consumer1.join();
//        consumer2.join();
    }

    private static class Producer implements Runnable{
        BlockingQueueSyncImpl<Integer> bq;
        public Producer(BlockingQueueSyncImpl<Integer> bq) {
            this.bq = bq;
        }
        @Override
        public void run() {
            for(int i=0; i<100; i++) {
                try {
                    bq.enqueue(i);
//                    Thread.sleep(10);
                    System.out.println("produced: " + i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Consumer  implements Runnable {
        BlockingQueueSyncImpl<Integer> bq;
        public Consumer(BlockingQueueSyncImpl<Integer> bq) {
            this.bq = bq;
        }

        @Override
        public void run() {
            for(int i=0; i<50; i++) {
                try {
                    System.out.println("consumed: " + bq.dequeue() + " by: " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
