package nasirov.yv.ac.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;

/**
 * @author Nasirov Yuriy
 */
public class AwareHelper {

	private final Lock lock = new ReentrantLock();

	private final Condition condition = lock.newCondition();

	private volatile boolean mark = false;

	@SneakyThrows
	public void await() {
		lock.lock();
		while (!mark) {
			condition.await();
		}
		mark = false;
		lock.unlock();
	}

	public void signal() {
		lock.lock();
		mark = true;
		condition.signalAll();
		lock.unlock();
	}
}
