import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Main {
    public static void main( String args[] ) throws Exception{
        final BQ<Integer> q = new BQ<>(5);

        Thread t1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 50; i++) {
                        q.enqueue(new Integer(i));
                        System.out.println("enqueued " + i);
                    }
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread t2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 25; i++) {
                        System.out.println("Thread 2 dequeued: " + q.dequeue());
                    }
                } catch (InterruptedException ie) {

                }
            }
        });

        Thread t3 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    for (int i = 0; i < 25; i++) {
                        System.out.println("Thread 3 dequeued: " + q.dequeue());
                    }
                } catch (InterruptedException ie) {

                }
            }
        });

        t1.start();
        Thread.sleep(4000);
        t2.start();

        t2.join();

        t3.start();
        t1.join();
        t3.join();
    }
}

class BQ<T> {
    Queue<T> queue;
    int capacity;
    ReentrantLock lock = new ReentrantLock();
    Condition minSizeCondition;
    Condition maxSizeCondition;
    BQ(int capacity) {
        queue = new LinkedList<T>();
        this.capacity = capacity;
        minSizeCondition = lock.newCondition();
        maxSizeCondition = lock.newCondition();
    }

    public void enqueue(T item) throws InterruptedException {
        lock.lock();
        while(queue.size()==capacity) {
            maxSizeCondition.await();
        }
        queue.add(item);
        minSizeCondition.signalAll();
        lock.unlock();
    }

    public T dequeue() throws InterruptedException {
        lock.lock();
        while(queue.size()==0) {
            minSizeCondition.await();
        }
        T item = queue.poll();
        maxSizeCondition.signalAll();
        lock.unlock();
        return item;
    }
}
