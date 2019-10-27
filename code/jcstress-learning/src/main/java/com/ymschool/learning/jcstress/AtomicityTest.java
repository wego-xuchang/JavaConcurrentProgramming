package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.J_Result;

import java.util.concurrent.atomic.AtomicInteger;


public class AtomicityTest {

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "See initial value while writer not finished.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "See full value while writer finished")
    @Outcome(expect = Expect.FORBIDDEN, desc = "partial values are forbidden.")
    @State
    public static class IntegerAtomicity {
        int v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF;
        }


        @Actor
        public void reader(I_Result r) {
            r.r1 = v;

        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "See initial value while writer not finished.")
    @Outcome(id = "65535", expect = Expect.ACCEPTABLE, desc = "See full value while writer0 finished.")
    @Outcome(id = "-65536", expect = Expect.ACCEPTABLE, desc = "See full value while writer1 finished.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Partial values are forbidden even in case of concurrent update.")
    @State
    public static class IntegerConcurrentAtomicity {
        int v;

        @Actor
        public void writer0() {
            v = 0x0000FFFF;
        }

        @Actor
        public void writer1() {
            v = 0xFFFF0000;
        }


        @Actor
        public void reader(I_Result r) {
            r.r1 = v;

        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "See initial value while writer not finished.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "See full value while writer finished.")
    @Outcome(expect = Expect.ACCEPTABLE_INTERESTING, desc = "Partial values violate access atomicity, but allowed under JLS.")
    @State
    public static class LongAtomicity {
        long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }


    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "See initial value while writer not finished.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "See full value while writer finished.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Partial values are forbidden.")
    @State
    public static class VolatileLongAtomicity {
        volatile long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }


    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "One update lost.")
    @Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both updates.")
    @State
    public static class PlainIncrement {
        int v;

        @Actor
        public void actor1() {
            v++;
        }

        @Actor
        public void actor2() {
            v++;
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = v;
        }
    }


    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "One update lost.")
    @Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both updates.")
    @State
    public static class VolatileIncrement {
        volatile int v;

        @Actor
        public void actor1() {
            v++;
        }

        @Actor
        public void actor2() {
            v++;
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = v;
        }
    }

    @JCStressTest
    @Outcome(id = "1", expect = Expect.FORBIDDEN, desc = "One update lost.")
    @Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both updates.")
    @State
    public static class SynchronizedIncrement {
        int v;

        @Actor
        public void actor1() {
            synchronized (this) {
                v++;
            }
        }

        @Actor
        public void actor2() {
            synchronized (this) {
                v++;
            }
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = v;
        }
    }

    @JCStressTest
    @Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "One update lost.")
    @Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both updates.")
    @State
    public static class AtomicIntegerIncrement {
        AtomicInteger v = new AtomicInteger();

        @Actor
        public void actor1() {
            v.getAndIncrement();
        }

        @Actor
        public void actor2() {
            v.getAndIncrement();
        }

        @Arbiter
        public void arbiter(I_Result r) {
            r.r1 = v.get();
        }
    }

    @JCStressTest
    @Outcome(id = "100, 0", expect = Expect.ACCEPTABLE, desc = "Transfer not start yet.")
    @Outcome(id = "0, 100", expect = Expect.ACCEPTABLE, desc = "Transfer completed.")
    @Outcome(id = "0, 0", expect = Expect.ACCEPTABLE_INTERESTING, desc = "See partial result.")
    @Outcome(id = "100, 100", expect = Expect.ACCEPTABLE_INTERESTING, desc = "See partial result.")
    @State
    public static class PlainTransfer {
        int a = 100;
        int b;

        @Actor
        public void actor1() {
            a = a - 100;
            b = b + 100;
        }

        @Actor
        public void arbiter(II_Result r) {
            r.r1 = a;
            r.r2 = b;
        }
    }

    @JCStressTest
    @Outcome(id = "100, 0", expect = Expect.ACCEPTABLE, desc = "Transfer not start yet.")
    @Outcome(id = "0, 100", expect = Expect.ACCEPTABLE, desc = "Transfer completed.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Forbidden case.")
    @State
    public static class SynchronizedTransfer {
        int a = 100;
        int b;

        @Actor
        public void actor1() {
            synchronized (this) {
                a = a - 100;
                b = b + 100;
            }
        }

        @Actor
        public void arbiter(II_Result r) {
            synchronized (this) {
                r.r1 = a;
                r.r2 = b;
            }
        }
    }
}
