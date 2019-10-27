package com.ymschool.learning.jcstress;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MyLock implements Lock {

    private boolean locked = false;
    private Object lock = new Object();


    @Override
    public void lock() {
        synchronized (lock) {
            while (locked) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            locked = true;
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        synchronized (lock) {
            while (locked) {
                lock.wait();
            }

            locked = true;
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
            locked = false;
            lock.notifyAll();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}
