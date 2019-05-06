
/**
 * @author zg6197
 *
 */
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * This program looks for prime numbers with a given number of bits,
 * using multiple worker threads to test candidate numbers.  Each prime
 * is printed out when it is found.  The program runs for a given number
 * of minutes, then exits.  Note that the numbers are actually only 
 * "probably prime", but with a very high probability of 1 - 2^(-N), where
 * N is a given certainty factory.
 */
public class SupervisorThread {
	/**
	 * The program searches for prime numbers with this many bits.
	 * Note that with BITS set equal to 2048, the program will find
	 * primes fairly quickly.
	 */
	final static int BITS = 2048;
	/**
	 * Certainty factor:  A probable prime found by this program has
	 * a probability oft 1 - (2 raised to the power minus CERTAINTY)
	 * of being prime.
	 */
	final static int CERTAINTY = 100; 
	/**
	 * Number of minutes for the program to run.  The time will actually
	 * be a little longer:  At the end, the program waits for threads
	 * to complete their current tasks before exiting.
	 */
	private final static double MINUTES = 1.0;
	/**
	 * Number of worker threads to use.
	 */
	public final static int WORKERS = 4;
	public static ArrayBlockingQueue<BigInteger> queue; // queue of candidates to be tested
	public static volatile boolean running = true;  // will be set to false to terminate threads
	public static int primeCount = 0;  // number of primes found
	public static final BigInteger TWO = new BigInteger("2");  // 2, as a BigInteger
	
	
	
	
	private static class Supervisor extends Thread {
		Random rand = new Random();
		public void run() {
			BigInteger n = pickStartValue();
			while (running) {
				try {
					queue.put(n);
					if (Math.random() < 0.001)
						n = n.add(TWO);
					else
						n = pickStartValue();
				}
				catch (InterruptedException e) {
				}
			}
		}
		private BigInteger pickStartValue() {
			BigInteger n = new BigInteger(BITS, rand);
			n = n.setBit(0); // Make sure n is odd.
			n = n.setBit(BITS-1); // Make sure n has BITS bits.
			return n;
		}
	}
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		queue = new ArrayBlockingQueue<>(20);
		Workers[] worker = new Workers[WORKERS];
		Socket socket = null;
	  ServerSocket server = new ServerSocket(8886);
		 int count = 0;
		 System.out.println("***server start listening, waiting the clients***");
     // while loop to listen different clients
		for (int i = 0; i < WORKERS; i++) {
			socket = server.accept();
			worker[i] = new Workers(socket);
			worker[i].start();
			count++;// 客户端数量增加
      System.out.println("The worker number:" + count);
      InetAddress address = server.getInetAddress();
      System.out.println("the IP address of this worker：" + address.getHostAddress());
      }
		
		
		Timer timer = new Timer(true); // the timer that will stop the threads 
		// the parmeter makes the times use a daemon tread
		Supervisor boss = new Supervisor();
		long start = System.currentTimeMillis();
		boss.start();
		timer.schedule(new TimerTask() {
			    // This task's run() method will be called when the specified
			    // number of minutes have passed.  It sets the global variable
			    // running to false to signal all the threads the exits.
			    // It interrupts the supervisor thread, since it is probably
			    // blocked and needs to be woken up so that it can exit.
			    // To be safe, it also interrupts the worker threads, although
			    // that is probably not necessary.
			public void run() {
				running = false;
				boss.interrupt();
				for (Workers w : worker)
					w.interrupt();
			}
		}, (int)(60000*MINUTES));
		try {  // Wait for all threads to finish.
			boss.join();
			for (Workers w : worker)
				w.join();
		}
		catch (InterruptedException e) {
		}
		double time = (System.currentTimeMillis() - start)/60000.0 ;
		System.out.printf("%n%n%d primes found in %1.3f minutes.%n%n", primeCount, time);
	}
	

	/**
	 * Called by a worker thread when it finds a probably prime.
	 */
	synchronized static void foundPrime(BigInteger n) {
		primeCount++;
		System.out.printf("%nFound %d-bit prime number %d:%n",
				BITS, primeCount);
		System.out.printf("     %s%n%n", n.toString());
	}
	
}
	
	
