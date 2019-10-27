package com.ymschool.learning.jcstress;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

/*
    This is our first concurrency test. It is deliberately simplistic to show
    testing approaches, introduce JCStress APIs, etc.

    Suppose we want to see if the field increment is atomic. We can make test
    with two actors, both actors incrementing the field and recording what
    value they observed into the result object. As JCStress runs, it will
    invoke these methods on the objects holding the field once per each actor
    and instance, and record what results are coming from there.

    Done enough times, we will get the history of observed results, and that
    would tell us something about the concurrent behavior. For example, running
    this test would yield:

          [OK] o.o.j.t.JCStressSample_01_Simple
        (JVM args: [-server])
      Observed state   Occurrences   Expectation  Interpretation
                1, 1    54,734,140    ACCEPTABLE  Both threads came up with the same value: atomicity failure.
                1, 2    47,037,891    ACCEPTABLE  actor1 incremented, then actor2.
                2, 1    53,204,629    ACCEPTABLE  actor2 incremented, then actor1.

     How to run this test:
       $ java -jar jcstress-samples/target/jcstress.jar -t JCStress_APISample_01_Simple
这是我们的第一个并发性测试。这是故意简单化的表现
测试方法，介绍JCStress api，等等。
假设我们想看看字段增量是否是原子的。我们可以进行测试
使用两个参与者，两个参与者都增加字段并记录什么
它们在result对象中观察到的值。当JCStress运行时，它会
对每个参与者在持有字段的对象上调用这些方法一次
实例，并记录从那里得到的结果。
如果做得足够多，我们就会得到观察结果的历史记录
会告诉我们一些关于并发行为的信息。例如,跑步
这个测试的结果是:
[好]o.o.j.t.JCStressSample_01_Simple
(JVM参数:[- server])
观测状态出现期望解释
两个线程都得到了相同的值:原子性失败。
1、2、47,037,891可接受的actor1递增，然后actor2。
2、1 53,204,629可接受演员2递增，然后是演员1。
如何运行这个测试:
$ java -jar jcstress-samples/target/jcstress.jar -t jc_apisample_01_simple
 */

// Mark the class as JCStress test.
@JCStressTest

// These are the test outcomes.
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Both actors came up with the same value: atomicity failure.")
@Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "actor1 incremented, then actor2.")
@Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "actor2 incremented, then actor1.")

// This is a state object
@State
public class APISample_01_Simple {

    int v;

    @Actor
    public void actor1(II_Result r) {
        r.r1 = ++v; // record result from actor1 to field r1
    }

    @Actor
    public void actor2(II_Result r) {
        r.r2 = ++v; // record result from actor2 to field r2
    }

}