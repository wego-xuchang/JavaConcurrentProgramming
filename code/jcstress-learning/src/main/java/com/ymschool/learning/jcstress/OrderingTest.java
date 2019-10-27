package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.ZI_Result;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

@Outcome(id = "false, 0", expect = ACCEPTABLE, desc = "actor2 reads before actor1.")
@Outcome(id = "true, 1", expect = ACCEPTABLE, desc = "actor2 reads after actor1.")
@Outcome(id = "false, 1", expect = ACCEPTABLE, desc = "actor1 write i, actor2 read iSet, actor2 read i, actor1 write iSet")
@Outcome(id = "true, 0", expect = ACCEPTABLE_INTERESTING, desc = "REORDERING, actor1 write iSet, actor2 read iSet, actor2 read i, actor1 write i")
public class OrderingTest {

    @JCStressTest
    @JCStressMeta(OrderingTest.class)
    @State
    public static class PlainOrdering {
        int i;
        boolean iSet;

        @Actor
        public void actor1() {
            i = 1;
            iSet = true;
        }

        @Actor
        public void actor2(ZI_Result r) {
            r.r1 = iSet;
            r.r2 = i;
        }
    }

    @JCStressTest
    @JCStressMeta(OrderingTest.class)
    @State
    public static class VolatileOrdering {
        int i;
        volatile boolean iSet;

        @Actor
        public void actor1() {
            i = 1;
            iSet = true;
        }

        @Actor
        public void actor2(ZI_Result r) {
            r.r1 = iSet;
            r.r2 = i;
        }
    }

    @JCStressTest
    @JCStressMeta(OrderingTest.class)
    @State
    public static class SynchronizedOrdering {
        int i;
        boolean iSet;

        @Actor
        public void actor1() {
            synchronized (this) {
                i = 1;
                iSet = true;
            }
        }

        @Actor
        public void actor2(ZI_Result r) {
            synchronized (this) {
                r.r1 = iSet;
                r.r2 = i;
            }
        }
    }
}
