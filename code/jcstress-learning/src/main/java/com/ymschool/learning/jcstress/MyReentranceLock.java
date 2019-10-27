package com.ymschool.learning.jcstress;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyReentranceLock implements Lock {

    private boolean locked = false;
    private Object lock = new Object();
    private Thread lockOwner = null;
    private int count = 0;

    @Override
    public void lock() {
        synchronized (lock) {
            Thread currentThread = Thread.currentThread();

            while (locked && currentThread != lockOwner) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            locked = true;
            lockOwner = currentThread;
            count++;
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        synchronized (lock) {
            Thread currentThread = Thread.currentThread();

            while (locked) {
                lock.wait();
            }

            locked = true;
            lockOwner = currentThread;
            count++;
        }
    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        synchronized (lock) {
            if (lockOwner == Thread.currentThread()) {
                count--;

                if (count == 0) {
                    locked = false;
                    lock.notifyAll();
                }
            }
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
