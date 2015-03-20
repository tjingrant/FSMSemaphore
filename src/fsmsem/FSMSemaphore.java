package fsmsem;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class FSMSemaphore {
	private int _noOfStates;
	private int _noOfTypes;
	private Map<Semaphore, Integer> _typesWaiting;
	private int _currentState;
	private CustomSemaphore _cs;
	
	public interface Policy {
		public Semaphore next(Map<Semaphore, Integer[]> _sems);
	}
	
	public interface Decision {
		public boolean decide(int priority, int state);
	}
	
	Map<Semaphore, Integer[]> _barriers; //semaphore -> priority, state
	AtomicInteger _time;
	Semaphore _sem;
	Policy _pol;
	Decision _dec;
	
	public FSMSemaphore(int permits) {
		_barriers = new HashMap<Semaphore, Integer[]>();
		_sem = new Semaphore(permits);
		_pol = new Policy() {
			public Semaphore next(Map<Semaphore, Integer[]> _sems) {
				int min = Integer.MAX_VALUE;
				Semaphore target = null;
				for (Semaphore key : _sems.keySet()) {
				    int value = _sems.get(key)[0];
				    if (value < min) {
				    	min = value;
				    	target = key;
				    }
				}
				return target;
			}
		};
		_dec = new Decision() {
			public boolean decide(int val) {
				return true;
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
			_barriers.put(threadBarrier, new Integer[] {new Integer(priority),
														new Integer(-1)});
			//have to check here as it might just be the case
			//where the permit is readily available
			//it spawns the new thread because the current thread
			//is blocked by the barriers and the new thread will
			//unblock this parent thread when it comes to its turn.
			
			//a barrier and a semaphore exists as a inter-thread 
			//communication mechanism.
			
			threadBarrier.acquire(); //to block the current thread
									 //until it receives a "go!"
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void checkAndRelase() {
		(new Thread() {
			  public void run() {
					try {
						//uses policy specified by the user
						//to get the next barrier lifted
						Semaphore barrier = null;
						int val, state;
						synchronized(_barriers) {
							barrier = _pol.next(_barriers);
							val = _barriers.get(barrier)[0];
							state = _barriers.get(barrier)[1];
							_barriers.remove(barrier);
						}
						if (barrier != null && _dec.decide(val, state)) {
							_sem.acquire();
							barrier.release();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			  }
		}).start();
	}
		
	
	public void acquire(int state, int type, int priority) {
		
	}
	
}
