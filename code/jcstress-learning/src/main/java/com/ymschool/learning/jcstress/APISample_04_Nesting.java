package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

/*
    It is sometimes convenient to put the tests in the same source file for
    better comparison. JCStress allows to nest tests like this:

    How to run this test:
       $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_04_Nesting
 */

public class APISample_04_Nesting {

    @JCStressTest
    @Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
    @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "actor1 incremented, then actor2.")
    @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "actor2 incremented, then actor1.")
    @State
    public static class PlainTest {
        int v;

        @Actor
        public void actor1(II_Result r) {
            r.r1 = ++v;
        }

        @Actor
        public void actor2(II_Result r) {
            r.r2 = ++v;
        }
    }

    @JCStressTest
    @Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
    @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "actor1 incremented, then actor2.")
    @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "actor2 incremented, then actor1.")
    @State
    public static class VolatileTest {
        volatile int v;

        @Actor
        public void actor1(II_Result r) {
            r.r1 = ++v;
        }

        @Actor
        public void actor2(II_Result r) {
            r.r2 = ++v;
        }
    }

}
