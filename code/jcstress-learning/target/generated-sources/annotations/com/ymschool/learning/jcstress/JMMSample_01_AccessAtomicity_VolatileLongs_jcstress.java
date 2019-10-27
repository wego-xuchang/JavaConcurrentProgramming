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
import com.ymschool.learning.jcstress.JMMSample_01_AccessAtomicity.VolatileLongs;
import org.openjdk.jcstress.infra.results.J_Result_jcstress;

public class JMMSample_01_AccessAtomicity_VolatileLongs_jcstress extends Runner<J_Result_jcstress> {

    volatile StateHolder<VolatileLongs, J_Result_jcstress> version;

    public JMMSample_01_AccessAtomicity_VolatileLongs_jcstress(TestConfig config, TestResultCollector collector, ExecutorService pool) {
        super(config, collector, pool, "com.ymschool.learning.jcstress.JMMSample_01_AccessAtomicity.VolatileLongs");
    }

    @Override
    public Counter<J_Result_jcstress> sanityCheck() throws Throwable {
        Counter<J_Result_jcstress> counter = new Counter<>();
        sanityCheck_API(counter);
        sanityCheck_Footprints(counter);
        return counter;
    }

    private void sanityCheck_API(Counter<J_Result_jcstress> counter) throws Throwable {
        final VolatileLongs s = new VolatileLongs();
        final J_Result_jcstress r = new J_Result_jcstress();
        Collection<Future<?>> res = new ArrayList<>();
        res.add(pool.submit(() -> s.writer()));
        res.add(pool.submit(() -> s.reader(r)));
        for (Future<?> f : res) {
            try {
                f.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        counter.record(r);
    }

    private void sanityCheck_Footprints(Counter<J_Result_jcstress> counter) throws Throwable {
        config.adjustStrides(size -> {
            version = new StateHolder<>(new VolatileLongs[size], new J_Result_jcstress[size], 2, config.spinLoopStyle);
            for (int c = 0; c < size; c++) {
                J_Result_jcstress r = new J_Result_jcstress();
                VolatileLongs s = new VolatileLongs();
                version.rs[c] = r;
                version.ss[c] = s;
                s.writer();
                s.reader(r);
                counter.record(r);
            }
        });
    }

    @Override
    public Counter<J_Result_jcstress> internalRun() {
        version = new StateHolder<>(new VolatileLongs[0], new J_Result_jcstress[0], 2, config.spinLoopStyle);

        control.isStopped = false;

        List<Callable<Counter<J_Result_jcstress>>> tasks = new ArrayList<>();
        tasks.add(this::writer);
        tasks.add(this::reader);
        Collections.shuffle(tasks);

        Collection<Future<Counter<J_Result_jcstress>>> results = new ArrayList<>();
        for (Callable<Counter<J_Result_jcstress>> task : tasks) {
            results.add(pool.submit(task));
        }

        try {
            TimeUnit.MILLISECONDS.sleep(config.time);
        } catch (InterruptedException e) {
        }

        control.isStopped = true;

        waitFor(results);

        Counter<J_Result_jcstress> counter = new Counter<>();
        for (Future<Counter<J_Result_jcstress>> f : results) {
            try {
                counter.merge(f.get());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return counter;
    }

    public final void jcstress_consume(StateHolder<VolatileLongs, J_Result_jcstress> holder, Counter<J_Result_jcstress> cnt, int a, int actors) {
        VolatileLongs[] ss = holder.ss;
        J_Result_jcstress[] rs = holder.rs;
        int len = ss.length;
        int left = a * len / actors;
        int right = (a + 1) * len / actors;
        for (int c = left; c < right; c++) {
            J_Result_jcstress r = rs[c];
            VolatileLongs s = ss[c];
            s.v = 0;
            cnt.record(r);
            r.r1 = 0;
        }
    }

    public final void jcstress_updateHolder(StateHolder<VolatileLongs, J_Result_jcstress> holder) {
        if (!holder.tryStartUpdate()) return;
        VolatileLongs[] ss = holder.ss;
        J_Result_jcstress[] rs = holder.rs;
        int len = ss.length;

        int newLen = holder.updateStride ? Math.max(config.minStride, Math.min(len * 2, config.maxStride)) : len;

        VolatileLongs[] newS = ss;
        J_Result_jcstress[] newR = rs;
        if (newLen > len) {
            newS = Arrays.copyOf(ss, newLen);
            newR = Arrays.copyOf(rs, newLen);
            for (int c = len; c < newLen; c++) {
                newR[c] = new J_Result_jcstress();
                newS[c] = new VolatileLongs();
            }
         }

        version = new StateHolder<>(control.isStopped, newS, newR, 2, config.spinLoopStyle);
        holder.finishUpdate();
   }

    public final Counter<J_Result_jcstress> writer() {

        Counter<J_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<VolatileLongs,J_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            VolatileLongs[] ss = holder.ss;
            J_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                VolatileLongs s = ss[c];
                s.writer();
            }

            holder.postRun();

            jcstress_consume(holder, counter, 0, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

    public final Counter<J_Result_jcstress> reader() {

        Counter<J_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<VolatileLongs,J_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            VolatileLongs[] ss = holder.ss;
            J_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                VolatileLongs s = ss[c];
                J_Result_jcstress r = rs[c];
                r.trap = 0;
                s.reader(r);
            }

            holder.postRun();

            jcstress_consume(holder, counter, 1, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

}
