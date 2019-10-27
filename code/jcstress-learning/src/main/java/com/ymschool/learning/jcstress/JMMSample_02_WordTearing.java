package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.ZZ_Result;

import java.util.BitSet;

public class JMMSample_02_WordTearing {

    /*
      ----------------------------------------------------------------------------------------------------------

        Java Memory Model prohibits word tearing. That is, it mandates treating
        every field and array element as distinct, and the operations for one
        element should not disturb others.

              [OK] org.openjdk.jcstress.samples.JMMSample_02_WordTearing.JavaArrays
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
              true, true   228,447,200    ACCEPTABLE  Seeing both updates intact.
    */

    @JCStressTest
    @Outcome(id = "true, true", expect = Expect.ACCEPTABLE, desc = "Seeing both updates intact.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class JavaArrays {
        boolean[] bs = new boolean[2];

        @Actor
        public void writer1() {
            bs[0] = true;
        }

        @Actor
        public void writer2() {
            bs[1] = true;
        }

        @Arbiter
        public void arbiter(ZZ_Result r) {
            r.r1 = bs[0];
            r.r2 = bs[1];
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        However, while that requirement is enforced for fields and array elements, the
        implementations may still violate this requirement, if, say, they pack elements
        densely, and read/write adjacent elements routinely.

              [OK] org.openjdk.jcstress.samples.JMMSample_02_WordTearing.BitSets
            (JVM args: [-server])
          Observed state   Occurrences              Expectation  Interpretation
             false, true     1,107,454   ACCEPTABLE_INTERESTING  Destroyed one update.
             true, false     1,297,199   ACCEPTABLE_INTERESTING  Destroyed one update.
              true, true   147,209,607               ACCEPTABLE  Seeing both updates intact.
     */

    @JCStressTest
    @Outcome(id = "true, true",  expect = Expect.ACCEPTABLE, desc = "Seeing both updates intact.")
    @Outcome(id = "false, true", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Destroyed one update.")
    @Outcome(id = "true, false", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Destroyed one update.")
    @State
    public static class BitSets {

        BitSet bs = new BitSet();

        @Actor
        public void writer1() {
            bs.set(0);
        }

        @Actor
        public void writer2() {
            bs.set(1);
        }

        @Arbiter
        public void arbiter(ZZ_Result r) {
            r.r1 = bs.get(0);
            r.r2 = bs.get(1);
        }
    }

}