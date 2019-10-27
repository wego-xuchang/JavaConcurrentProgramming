package com.ymschool.learning.jcstress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openjdk.jcstress.infra.runners.TestConfig;
import org.openjdk.jcstress.infra.collectors.TestResultCollector;
import org.openjdk.jcstress.infra.runners.Runner;
import org.openjdk.jcstress.infra.runners.StateHolder;
import org.openjdk.jcstress.util.Counter;
import org.openjdk.jcstress.vm.WhiteBoxSupport;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Callable;
import java.util.Collections;
import java.util.List;
import com.ymschool.learning.jcstress.VisibilityTest.PlainReadWrite;

public class VisibilityTest_PlainReadWrite_jcstress extends Runner<VisibilityTest_PlainReadWrite_jcstress.Outcome> {

    public VisibilityTest_PlainReadWrite_jcstress(TestConfig config, TestResultCollector collector, ExecutorService pool) {
        super(config, collector, pool, "com.ymschool.learning.jcstress.VisibilityTest.PlainReadWrite");
    }

    @Override
    public void run() {
        Counter<Outcome> results = new Counter<>();

        for (int c = 0; c < config.iters; c++) {
            try {
                WhiteBoxSupport.tryDeopt(config.deoptRatio);
            } catch (NoClassDefFoundError err) {
                // gracefully "handle"
            }

            run(results);

            if (results.count(Outcome.STALE) > 0) {
                messages.add("Have stale threads, forcing VM to exit for proper cleanup.");
                dump(c, results);
                System.exit(0);
            } else {
                dump(c, results);
            }
        }
    }

    @Override
    public Counter<Outcome> sanityCheck() throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public Counter<Outcome> internalRun() {
        throw new UnsupportedOperationException();
    }

    private void run(Counter<Outcome> results) {
        long target = System.currentTimeMillis() + config.time;
        while (System.currentTimeMillis() < target) {

            final PlainReadWrite state = new PlainReadWrite();
            final Holder holder = new Holder();

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        holder.started = true;
                        state.actor1();
                    } catch (Exception e) {
                        holder.error = true;
                    }
                    holder.terminated = true;
                }
            });
            t1.setDaemon(true);
            t1.start();

            while (!holder.started) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }

            try {
                state.signal();
            } catch (Exception e) {
                holder.error = true;
            }

            try {
                t1.join(Math.max(2*config.time, Runner.MIN_TIMEOUT_MS));
            } catch (InterruptedException e) {
                // do nothing
            }

            if (holder.terminated) {
                if (holder.error) {
                    results.record(Outcome.ERROR);
                } else {
                    results.record(Outcome.TERMINATED);
                }
            } else {
                results.record(Outcome.STALE);
                return;
            }
        }
    }

    private static class Holder {
        volatile boolean started;
        volatile boolean terminated;
        volatile boolean error;
    }

    public enum Outcome {
        TERMINATED,
        STALE,
        ERROR,
    }
}
