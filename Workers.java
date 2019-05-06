import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;

/**
 * @author zg6197
 */

/**
 * This class defined the worker threads that test numbers for primality. The
 * thread runs in a loop, taking numbers from the queue of candidate numbers and
 * testing them. The thread exits when the global volatile variable running is
 * set to false, but it will complete work on the current candidate before
 * exiting.
 */
public class Workers extends Thread {
	Socket socket = null;
	SupervisorThread boss;
	public Workers ( Socket socket ) {
		this.socket = socket;
	}

	public void run () {
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		OutputStream os = null;
		PrintWriter pw = null;
		try {
			is = socket.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			String info = null;
			
				
			
      // 4.获取输出流
      os = socket.getOutputStream();
      pw = new PrintWriter(os);// 包装打印流
      pw.write("i recieved message, thx");
      pw.flush();
  
      
			while ( SupervisorThread.running ||(info = br.readLine()) != null) {
				System.out.println("I am a worker, here:" + info);
				try {
					BigInteger n = SupervisorThread.queue.take();
					if ( n.isProbablePrime(SupervisorThread.CERTAINTY) ) {
						SupervisorThread.foundPrime(n);
					}
				} catch ( InterruptedException e ) {}
			}
		} catch ( IOException e1 ) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
// end ThreadedBigPrimes