import java.util.*;

public class Scheduler extends Thread
{
	private static final int numOfQueues = 3;
    private Vector<Vector<TCB>> queues;  	// array of queue 0, 1, 2
	private int timeSlice;		// time quantum for queue 0, 1, 2 respectively
	private int q; // current q number 0, 1, or 2

    private static final int DEFAULT_TIME_SLICE = 500;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
    private void initTid(int maxThreads) {
		tids = new boolean[maxThreads];
		for (int i = 0; i < maxThreads; i++)
			tids[i] = false;
	}

    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid() {
		for (int i = 0; i < tids.length; i++) {
			int tentative = (nextId + i) % tids.length;
			if (tids[tentative] == false) {
				tids[tentative] = true;
				nextId = (tentative + 1) % tids.length;
				return tentative;
			}
		}
		return -1;
	}

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid(int tid) {
		if (tid >= 0 && tid < tids.length && tids[tid]) {
			tids[tid] = false;
			return true;
		}
		return false;
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    public TCB getMyTcb() {
		Thread myThread = Thread.currentThread(); // Get my thread object
		for (int j = 0; j < numOfQueues; j++) {
			synchronized(queues.get(j)) {
				for (int i = 0; i < queues.get(j).size(); i++) {
					TCB tcb = (TCB)(queues.get(j).get(i));
					Thread thread = tcb.getThread();
					if (thread == myThread) // if this is my TCB, return it
						return tcb;
					}
				}
		}
		return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads() {
		return tids.length;
    }

    public Scheduler() {
		queues = new Vector<Vector<TCB>>(numOfQueues);
		timeSlice = DEFAULT_TIME_SLICE;
		q = 0;

		for (int i = 0 ; i < numOfQueues; i++) {
			queues.add(new Vector<TCB>());
		}
		
		initTid(DEFAULT_MAX_THREADS);
    }

    public Scheduler(int quantum) {
		queues = new Vector<Vector<TCB>>(numOfQueues);
		timeSlice = DEFAULT_TIME_SLICE;
		q = 0;

		for (int i = 0 ; i < numOfQueues; i++) {
			queues.add(new Vector<TCB>());
		}
		initTid(DEFAULT_MAX_THREADS);
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler(int quantum, int maxThreads) {
		queues = new Vector<Vector<TCB>>(numOfQueues);
		timeSlice = DEFAULT_TIME_SLICE;
		q = 0;

		for (int i = 0 ; i < numOfQueues; i++) {
			queues.add(new Vector<TCB>());
		}
		initTid(maxThreads);
    }

    private void schedulerSleep() {
		try {
			Thread.sleep(timeSlice);
		} catch (InterruptedException e) {
		}
    }

    // A modified addThread of p161 example
    public TCB addThread(Thread t) {

		TCB parentTcb = getMyTcb(); // get my TCB and find my TID
		int pid = (parentTcb != null) ? parentTcb.getTid() : -1;
		int tid = getNewTid(); // get a new TID
		if (tid == -1)
			return null;
		TCB tcb = new TCB(t, tid, pid); // create a new TCB
		queues.get(0).add(tcb); // right?
		return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread() {
		TCB tcb = getMyTcb(); 
		if (tcb!= null)
			return tcb.setTerminated();
		else
			return false;
    }

    public void sleepThread(int milliseconds) {
		try {
			sleep(milliseconds);
		} catch (InterruptedException e) { }
    }
    
    // A modified run of p161
    public void run() {
		Thread current = null;
	
		while (true) {
			try {

				while (queues.get(q).isEmpty()){
					if (q < 2) 
						q++;
					else 
						q = 0;	
				}

				TCB currentTCB = (TCB)queues.get(q).firstElement();
				if (currentTCB.getTerminated()) {
					queues.get(q).remove(currentTCB);
					returnTid(currentTCB.getTid());
					continue;
				} 

				current = currentTCB.getThread();
				if (current != null) {
					if (current.isAlive())
						current.resume(); 
					else {
						current.start(); 
					}
				}

				if (q == 0) {
					schedulerSleep();
				} else {
					preemptiveSleep(current);
				}

				synchronized (queues.get(q)) {
					if (current != null && current.isAlive())
						current.suspend(); 

					queues.get(q).remove(currentTCB); // rotate this TCB to the end
					int nextQueue = (q < 2)? q + 1 : 2; 
					queues.get(nextQueue).add(currentTCB);
					q = 0;
				}

			} catch (NullPointerException e3) { };
		}
	}

	// returns true if it should be interrupted
	private void preemptiveSleep(Thread current) {
		if (q == 1) {
			schedulerSleep();
			if (!queues.get(0).isEmpty()) {	
				if (current != null && current.isAlive()){
					current.suspend(); 
				}	
				q = 0;
				run();
				System.out.print("EXITED the FOREVER LOOP!!!!");

				q = 1;
				current.resume();	
			}
			schedulerSleep();

		} else {
			for (int i = 0 ; i < 3 ; i++) {
				schedulerSleep();

				if (!queues.get(0).isEmpty()) {
					if (current != null && current.isAlive()){
						current.suspend(); 
					}	
					q = 0;
					run();
					System.out.print("EXITED the FOREVER LOOP!!!!");

				} else if (!queues.get(1).isEmpty()) {
					if (current != null && current.isAlive()){
						current.suspend(); 
					}
					q = 1;
					run();
					System.out.print("EXITED the FOREVER LOOP!!!!");

				} 
			}
			q = 2;
			current.resume();	
			schedulerSleep();
		}
	}
}