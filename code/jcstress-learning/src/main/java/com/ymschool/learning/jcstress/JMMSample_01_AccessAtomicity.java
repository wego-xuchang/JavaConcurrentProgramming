package com.ymschool.learning.jcstress;

/*
 * Copyright (c) 2016, Red Hat Inc.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import org.openjdk.jcstress.infra.results.J_Result;

public class JMMSample_01_AccessAtomicity {

    /*
      ----------------------------------------------------------------------------------------------------------

        This is our first case: access atomicity. Most basic types come with an
        intuitive property: the reads and the writes of these basic types happen
        in full, even under races:

              [OK] org.openjdk.jcstress.samples.JMMSample_01_AccessAtomicity.Integers
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                      -1   221,268,498    ACCEPTABLE  Seeing the full value.
                       0    17,764,332    ACCEPTABLE  Seeing the default value: writer had not acted yet.

    */

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class Integers {
        int v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF;
        }

        @Actor
        public void reader(I_Result r) {
            r.r1 = v;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        There are a few interesting exceptions codified in Java Language Specification,
        under 17.7 "Non-Atomic Treatment of double and long". It says that longs and
        doubles could be treated non-atomically.

        NOTE: This test would yield interesting results on 32-bit VMs.

               [OK] org.openjdk.jcstress.samples.JMMSample_01_AccessAtomicity.Longs
            (JVM args: [-server])
          Observed state   Occurrences              Expectation  Interpretation
                      -1   181,716,629               ACCEPTABLE  Seeing the full value.
             -4294967296        40,481   ACCEPTABLE_INTERESTING  Other cases are violating access atomicity, but allowed u...
                       0    10,439,305               ACCEPTABLE  Seeing the default value: writer had not acted yet.
              4294967295         2,545   ACCEPTABLE_INTERESTING  Other cases are violating access atomicity, but allowed u...
     */

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = Expect.ACCEPTABLE_INTERESTING, desc = "Other cases are violating access atomicity, but allowed under JLS.")
    @Ref("https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.7")
    @State
    public static class Longs {
        long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }

    /*
      ----------------------------------------------------------------------------------------------------------

        Recovering the access atomicity is possible with making the field "volatile":

               [OK] org.openjdk.jcstress.samples.JMMSample_01_AccessAtomicity.VolatileLongs
            (JVM args: [-server])
          Observed state   Occurrences   Expectation  Interpretation
                      -1    25,920,268    ACCEPTABLE  Seeing the full value.
                       0   101,853,902    ACCEPTABLE  Seeing the default value: writer had not acted yet.
     */

    @JCStressTest
    @Outcome(id = "0", expect = Expect.ACCEPTABLE, desc = "Seeing the default value: writer had not acted yet.")
    @Outcome(id = "-1", expect = Expect.ACCEPTABLE, desc = "Seeing the full value.")
    @Outcome(expect = Expect.FORBIDDEN, desc = "Other cases are forbidden.")
    @State
    public static class VolatileLongs {
        volatile long v;

        @Actor
        public void writer() {
            v = 0xFFFFFFFF_FFFFFFFFL;
        }

        @Actor
        public void reader(J_Result r) {
            r.r1 = v;
        }
    }


}
