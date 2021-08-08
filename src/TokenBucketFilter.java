import java.util.LinkedList;
import java.util.List;

public class TokenBucketFilter {
    public static void main(String[] args) throws InterruptedException {
        int rate = 1;
        Filter filter = new Filter(10, rate);
        Thread daemon = new Thread(new DaemonTask(rate, filter));
        daemon.start();
        Thread.sleep(5000);
        List<Thread> threadList = new LinkedList<>();
        for(int i=0 ; i<5; i++) {
            Thread worker = new Thread(() -> {
                try {
                    filter.getToken();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            worker.setName("Thread_"+(i+1));
            threadList.add(worker);
            System.out.println("Starting " + worker.getName());
            worker.start();
            System.out.println("Joning " + worker.getName());
            worker.join();
            System.out.println("Joining completed for " + worker.getName());
        }
        daemon.interrupt();
        System.out.println("isInterrupt for deamon out is : " + daemon.isInterrupted());
        //daemon.join();
    }
}

class Filter {
    int N;
    int currentToken;
    int rate;
    Filter(int maxCount, int rate) {
        N = maxCount;
        currentToken=0;
        this.rate=rate;
    }
    public void incrementToken(){
        System.out.println("Incrementing for currentToken="+currentToken);
        if(currentToken<N) currentToken++;
    }
    public synchronized void getToken() throws InterruptedException {
        while(currentToken==0) {
            System.out.println("Waiting for token generation");
            Thread.sleep(rate*1000);
        }
        currentToken--;
        System.out.println("Granting " + Thread.currentThread().getName() + " token at " + (System.currentTimeMillis() / 1000));
    }
}

class DaemonTask implements Runnable {
    int rate;
    Filter filter;
    DaemonTask(int rate, Filter filter) {
        this.rate = rate;
        this.filter = filter;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            System.out.println("isInterrupt: " + Thread.currentThread().isInterrupted());
            synchronized (this) {
                filter.incrementToken();
            }
            System.out.println("Adding Token in Daemon thread");
            try {
                Thread.sleep(rate*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Daemon Thread Interrupted at " + (System.currentTimeMillis()/1000));
    }
}
