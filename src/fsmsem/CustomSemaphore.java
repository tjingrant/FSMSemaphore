package fsmsem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomSemaphore {
	
	public interface Policy {
		public Semaphore next(Map<Semaphore, Integer> _sems);
	}
	
	Map<Semaphore, Integer> _barriers;
	AtomicInteger _time;
	Semaphore _sem;
	Policy _pol;
	
	public CustomSemaphore(int permits) {
		_barriers = new HashMap<Semaphore, Integer>();
		_sem = new Semaphore(permits);
		_pol = new Policy() {
			public Semaphore next(Map<Semaphore, Integer> _sems) {
				int min = Integer.MAX_VALUE;
				Semaphore target = null;
				for (Semaphore key : _sems.keySet()) {
				    int value = _sems.get(key);
				    if (value < min) {
				    	min = value;
				    	target = key;
				    }
				}
				return target;
			}
		};
	}
	
	public void release() {
		_sem.release();
	}
	
	public void acquire(int priority) {
		//threaadBarrier will block the current thread 
		Semaphore threadBarrier = new Semaphore(1);
		try {
			threadBarrier.acquire();
			_barriers.put(threadBarrier, priority);
			//have to check here as it might just be the case
			//where the permit is readily available
			//it spawns the new thread because the current thread
			//is blocked by the barriers and the new thread will
			//unblock this parent thread when it comes to its turn.
			
			//a barrier and a semaphore exists as a inter-thread 
			//communication mechanism.
			(new Thread() {
				  public void run() {
						try {
							//uses policy specified by the user
							//to get the next barrier lifted
							Semaphore barrier = null;
							synchronized(_barriers) {
								barrier = _pol.next(_barriers);
								_barriers.remove(barrier);
							}
							if (barrier != null) {
								_sem.acquire();
								barrier.release();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				  }
			}).start();
			threadBarrier.acquire(); //to block the current thread
									 //until it receives a "go!"
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
}
