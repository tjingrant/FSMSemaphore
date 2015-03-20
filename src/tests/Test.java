package tests;
import fsmsem.CustomSemaphore;

public class Test {
	static CustomSemaphore cs = new CustomSemaphore(3);
	
	public static void main(String[] args) {
		
		System.out.println("Hello World");
		(new Thread() {
			  public void run() {
					cs.acquire(10);
					System.out.println("10");
					tryToSleep(1, 0.5);
					cs.release();
			  }
		}).start();
		(new Thread() {
			  public void run() {
					cs.acquire(5);
					System.out.println("5");
					tryToSleep(1, 0.5);
					cs.release();
			  }
		}).start();
		(new Thread() {
			  public void run() {
					cs.acquire(6);
					System.out.println("6");
					tryToSleep(1, 0.5);
					cs.release();
			  }
		}).start();
		(new Thread() {
			  public void run() {
					cs.acquire(6);
					System.out.println("1");
					tryToSleep(1, 0.5);
					cs.release();
			  }
		}).start();
		(new Thread() {
			  public void run() {
					cs.acquire(0);
					System.out.println("0");
					tryToSleep(1, 0.5);
					cs.release();
			  }
		}).start();
	}
	
	private static java.util.Random dice = new java.util.Random(); // random number generator, for delays mostly	
	public static void tryToSleep(double secMin, double secVar) {
        try {
            java.lang.Thread.sleep(Math.round(secMin*1000) + Math.round(dice.nextDouble()*(secVar)*1000));
        } catch (InterruptedException e) {
            System.out.println("Not Handling interruptions yet ... just going on with the program without as much sleep as needed ... how appropriate!");
        }
	}
}
