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
import com.ymschool.learning.jcstress.APISample_05_SharedMetadata.PlainTest;
import org.openjdk.jcstress.infra.results.II_Result_jcstress;

public class APISample_05_SharedMetadata_PlainTest_jcstress extends Runner<II_Result_jcstress> {

    volatile StateHolder<PlainTest, II_Result_jcstress> version;

    public APISample_05_SharedMetadata_PlainTest_jcstress(TestConfig config, TestResultCollector collector, ExecutorService pool) {
        super(config, collector, pool, "com.ymschool.learning.jcstress.APISample_05_SharedMetadata.PlainTest");
    }

    @Override
    public Counter<II_Result_jcstress> sanityCheck() throws Throwable {
        Counter<II_Result_jcstress> counter = new Counter<>();
        sanityCheck_API(counter);
        sanityCheck_Footprints(counter);
        return counter;
    }

    private void sanityCheck_API(Counter<II_Result_jcstress> counter) throws Throwable {
        final PlainTest s = new PlainTest();
        final II_Result_jcstress r = new II_Result_jcstress();
        Collection<Future<?>> res = new ArrayList<>();
        res.add(pool.submit(() -> s.actor1(r)));
        res.add(pool.submit(() -> s.actor2(r)));
        for (Future<?> f : res) {
            try {
                f.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        counter.record(r);
    }

    private void sanityCheck_Footprints(Counter<II_Result_jcstress> counter) throws Throwable {
        config.adjustStrides(size -> {
            version = new StateHolder<>(new PlainTest[size], new II_Result_jcstress[size], 2, config.spinLoopStyle);
            for (int c = 0; c < size; c++) {
                II_Result_jcstress r = new II_Result_jcstress();
                PlainTest s = new PlainTest();
                version.rs[c] = r;
                version.ss[c] = s;
                s.actor1(r);
                s.actor2(r);
                counter.record(r);
            }
        });
    }

    @Override
    public Counter<II_Result_jcstress> internalRun() {
        version = new StateHolder<>(new PlainTest[0], new II_Result_jcstress[0], 2, config.spinLoopStyle);

        control.isStopped = false;

        List<Callable<Counter<II_Result_jcstress>>> tasks = new ArrayList<>();
        tasks.add(this::actor1);
        tasks.add(this::actor2);
        Collections.shuffle(tasks);

        Collection<Future<Counter<II_Result_jcstress>>> results = new ArrayList<>();
        for (Callable<Counter<II_Result_jcstress>> task : tasks) {
            results.add(pool.submit(task));
        }

        try {
            TimeUnit.MILLISECONDS.sleep(config.time);
        } catch (InterruptedException e) {
        }

        control.isStopped = true;

        waitFor(results);

        Counter<II_Result_jcstress> counter = new Counter<>();
        for (Future<Counter<II_Result_jcstress>> f : results) {
            try {
                counter.merge(f.get());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return counter;
    }

    public final void jcstress_consume(StateHolder<PlainTest, II_Result_jcstress> holder, Counter<II_Result_jcstress> cnt, int a, int actors) {
        PlainTest[] ss = holder.ss;
        II_Result_jcstress[] rs = holder.rs;
        int len = ss.length;
        int left = a * len / actors;
        int right = (a + 1) * len / actors;
        for (int c = left; c < right; c++) {
            II_Result_jcstress r = rs[c];
            PlainTest s = ss[c];
            s.v = 0;
            cnt.record(r);
            r.r1 = 0;
            r.r2 = 0;
        }
    }

    public final void jcstress_updateHolder(StateHolder<PlainTest, II_Result_jcstress> holder) {
        if (!holder.tryStartUpdate()) return;
        PlainTest[] ss = holder.ss;
        II_Result_jcstress[] rs = holder.rs;
        int len = ss.length;

        int newLen = holder.updateStride ? Math.max(config.minStride, Math.min(len * 2, config.maxStride)) : len;

        PlainTest[] newS = ss;
        II_Result_jcstress[] newR = rs;
        if (newLen > len) {
            newS = Arrays.copyOf(ss, newLen);
            newR = Arrays.copyOf(rs, newLen);
            for (int c = len; c < newLen; c++) {
                newR[c] = new II_Result_jcstress();
                newS[c] = new PlainTest();
            }
         }

        version = new StateHolder<>(control.isStopped, newS, newR, 2, config.spinLoopStyle);
        holder.finishUpdate();
   }

    public final Counter<II_Result_jcstress> actor1() {

        Counter<II_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<PlainTest,II_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            PlainTest[] ss = holder.ss;
            II_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                PlainTest s = ss[c];
                II_Result_jcstress r = rs[c];
                r.trap = 0;
                s.actor1(r);
            }

            holder.postRun();

            jcstress_consume(holder, counter, 0, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

    public final Counter<II_Result_jcstress> actor2() {

        Counter<II_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<PlainTest,II_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            PlainTest[] ss = holder.ss;
            II_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                PlainTest s = ss[c];
                II_Result_jcstress r = rs[c];
                r.trap = 0;
                s.actor2(r);
            }

            holder.postRun();

            jcstress_consume(holder, counter, 1, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

}
