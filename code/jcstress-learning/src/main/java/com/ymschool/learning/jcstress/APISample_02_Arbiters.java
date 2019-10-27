package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

/*
    Another flavor of the same test as JCStress_APISample_01_Simple is using
    arbiters. Arbiters run after both actors, and therefore can observe the
    final result.

    This allows to directly observe the atomicity failure:

          [OK] org.openjdk.jcstress.samples.JCStress_APISample_02_Arbiters
        (JVM args: [-server])
      Observed state   Occurrences              Expectation  Interpretation
                   1       940,359   ACCEPTABLE_INTERESTING  One update lost: atomicity failure.
                   2   168,950,601               ACCEPTABLE  Actors updated independently.

    How to run this test:
       $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_02_Arbiters
 */

@JCStressTest

// These are the test outcomes.
@Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "One update lost: atomicity failure.")
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Actors updated independently.")
@State
public class APISample_02_Arbiters {

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