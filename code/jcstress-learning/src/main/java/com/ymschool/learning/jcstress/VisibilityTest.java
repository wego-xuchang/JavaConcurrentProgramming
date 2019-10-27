package com.ymschool.learning.jcstress;


import org.openjdk.jcstress.annotations.*;

@Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "write in signal is visible to actor1.")
@Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "write in signal is NOT visible to actor1.")
public class VisibilityTest {

    @JCStressTest(Mode.Termination)
    @JCStressMeta(VisibilityTest.class)
    @State
    public static class PlainReadWrite {
        int v;

        @Actor
        public void actor1() {
            while (v == 0) {
                // spin
            }
        }

        @Signal
        public void signal() {
            v = 1;
        }
    }

    @JCStressTest(Mode.Termination)
    @JCStressMeta(VisibilityTest.class)
    @State
    public static class VolatileReadWrite {
        volatile int v;

        @Actor
        public void actor1() {
            while (v == 0) {
                // spin
            }
        }

        @Signal
        public void signal() {
            v = 1;
        }
    }

    @JCStressTest(Mode.Termination)
    @JCStressMeta(VisibilityTest.class)
    @State
    public static class SynchronizedReadWrite {
        int v;

        @Actor
        public void actor1() {
            while (v == 0) {
                synchronized (this) {
                    if (v == 0) {
                        continue;//release lock
                    }
                }
            }
        }

        @Signal
        public void signal() {
            synchronized (this) {
                v = 1;
            }
        }
    }
}
