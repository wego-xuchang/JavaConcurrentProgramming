package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import static org.openjdk.jcstress.annotations.Expect.*;

public class JMMSample_04_PartialOrder {

    /*
      ----------------------------------------------------------------------------------------------------------

        The next property comes in relation to inter-thread semantics. In JMM,
        happens-before mandates what results are plausible and what are not, when
        non-synchronized reads are involved. That order is partial, so there are
        pairs of reads/writes we can tell nothing about order-wise.

        For example, in the case of two non-volatile variables, JMM allows observing
        "1, 0"!

              [OK] org.openjdk.jcstress.samples.JMMSample_04_PartialOrder.PlainReads
            (JVM args: [-server])
          Observed state   Occurrences              Expectation  Interpretation
                    0, 0     3,579,845               ACCEPTABLE  Doing both reads early.
                    0, 1        31,148               ACCEPTABLE  Caught in the middle: $x is visible, $y is not.
                    1, 0        23,841   ACCEPTABLE_INTERESTING  Seeing $y, but not $x!
                    1, 1   114,662,576               ACCEPTABLE  Doing both reads late.
    */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "Caught in the middle: $x is visible, $y is not.")
    @Outcome(id = "1, 0", expect = ACCEPTABLE_INTERESTING, desc = "Seeing $y, but not $x!")
    @State
    public static class PlainReads {
        int x;
        int y;

        @Actor
        public void actor1() {
            x = 1;
            y = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = y;
            r.r2 = x;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        The easiest way to solve this is to turn $v into volatile variable. In JMM,
        that would mean that the only execution which can justify (1, 0) is invalid:
        it has broken happens-before consistency. E.g. in the execution

          write(x, 1) --hb--> write(y, 1) --hb--> read(y):1 --hb--> read(x):1

        ...read(x) should have seen "1", not "0".

              [OK] org.openjdk.jcstress.samples.JMMSample_04_PartialOrder.VolatileGuard
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                    0, 0    99,074,452    ACCEPTABLE  Doing both reads early.
                    0, 1     2,309,155    ACCEPTABLE  Caught in the middle: $x is visible, $y is not.
                    1, 0             0     FORBIDDEN  Seeing $y, but not $x!
                    1, 1    43,441,703    ACCEPTABLE  Doing both reads late.
     */

    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = ACCEPTABLE, desc = "Caught in the middle: $x is visible, $y is not.")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Seeing $y, but not $x!")
    @State
    public static class VolatileGuard {

        int x;
        volatile int y;

        @Actor
        public void actor1() {
            x = 1;
            y = 1;
        }

        @Actor
        public void actor2(II_Result r) {
            r.r1 = y;
            r.r2 = x;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        Of course, the same thing is achievable with locks, except that (0, 1) is forbidden due to atomicity
        of the entire locked section.

              [OK] org.openjdk.jcstress.samples.JMMSample_04_PartialOrder.LockGuard
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                    0, 0    29,017,795    ACCEPTABLE  Doing both reads early.
                    0, 1             0     FORBIDDEN  Caught in the middle: $x is visible, $y is not.
                    1, 0             0     FORBIDDEN  Seeing $y, but not $x!
                    1, 1    31,223,995    ACCEPTABLE  Doing both reads late.

     */
    @JCStressTest
    @Outcome(id = "0, 0", expect = ACCEPTABLE, desc = "Doing both reads early.")
    @Outcome(id = "1, 1", expect = ACCEPTABLE, desc = "Doing both reads late.")
    @Outcome(id = "0, 1", expect = FORBIDDEN, desc = "Caught in the middle: $x is visible, $y is not.")
    @Outcome(id = "1, 0", expect = FORBIDDEN, desc = "Seeing $y, but not $x!")
    @State
    public static class LockGuard {

        int x;
        int y;

        @Actor
        public void actor1() {
            synchronized (this) {
                x = 1;
                y = 1;
            }
        }

        @Actor
        public void actor2(II_Result r) {
            synchronized (this) {
                r.r1 = y;
                r.r2 = x;
            }
        }
    }

    /*
       ----------------------------------------------------------------------------------------------------------

        Conclusion: the minimal inter-thread semantics (happens-before) is guaranteed for acquire/releases and
        volatiles. Anything weaker does not guarantee this effect

        Do inter-thread reads/writes form partial order?

          plain:                           no
          volatile:                       yes
     */

}
