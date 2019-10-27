package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

/*
    JCStress also allows to put the descriptions and references right at the test.
    This helps to identify the goal for the test, as well as the discussions about
    the behavior in question.

   How to run this test:
      $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_06_Descriptions
 */

@JCStressTest

// Optional test description
@Description("Sample Hello World test")

// Optional references. @Ref is repeatable.
@Ref("http://openjdk.java.net/projects/code-tools/jcstress/")

@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "actor1 incremented, then actor2.")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "actor2 incremented, then actor1.")
@State
public class APISample_06_Descriptions {

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