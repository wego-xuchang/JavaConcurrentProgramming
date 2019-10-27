package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

/*
   In many cases, tests share the outcomes and other metadata. To common them,
   there is a special @JCStressMeta annotation that says to look up the metadata
   at another class.

   How to run this test:
      $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_05_SharedMetadata
 */

@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "actor1 incremented, then actor2.")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "actor2 incremented, then actor1.")
public class APISample_05_SharedMetadata {

    @JCStressTest
    @JCStressMeta(APISample_05_SharedMetadata.class)
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
    @JCStressMeta(APISample_05_SharedMetadata.class)
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
