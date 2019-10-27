package com.ymschool.learning.jcstress;


import org.openjdk.jcstress.annotations.*;

/*
    Some concurrency tests are not following the "run continously" pattern. One
    of interesting test groups is that asserts if the code had terminated after
    a signal.

    Here, we use a single @Actor that busy-waits on a field, and a @Signal that
    sets that field. JCStress would start actor, and then deliver the signal.
    If it exits in reasonable time, it will record "TERMINATED" result, otherwise
    record "STALE".

    How to run this test:
       $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_03_Termination
 */

@JCStressTest(Mode.Termination)
@Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
@Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up.")
@State
public class APISample_03_Termination {

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