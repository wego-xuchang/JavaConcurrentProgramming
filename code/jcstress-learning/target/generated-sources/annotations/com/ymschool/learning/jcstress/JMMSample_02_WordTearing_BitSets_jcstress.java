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
import com.ymschool.learning.jcstress.JMMSample_02_WordTearing.BitSets;
import org.openjdk.jcstress.infra.results.ZZ_Result_jcstress;

public class JMMSample_02_WordTearing_BitSets_jcstress extends Runner<ZZ_Result_jcstress> {

    volatile StateHolder<BitSets, ZZ_Result_jcstress> version;

    public JMMSample_02_WordTearing_BitSets_jcstress(TestConfig config, TestResultCollector collector, ExecutorService pool) {
        super(config, collector, pool, "com.ymschool.learning.jcstress.JMMSample_02_WordTearing.BitSets");
    }

    @Override
    public Counter<ZZ_Result_jcstress> sanityCheck() throws Throwable {
        Counter<ZZ_Result_jcstress> counter = new Counter<>();
        sanityCheck_API(counter);
        sanityCheck_Footprints(counter);
        return counter;
    }

    private void sanityCheck_API(Counter<ZZ_Result_jcstress> counter) throws Throwable {
        final BitSets s = new BitSets();
        final ZZ_Result_jcstress r = new ZZ_Result_jcstress();
        Collection<Future<?>> res = new ArrayList<>();
        res.add(pool.submit(() -> s.writer1()));
        res.add(pool.submit(() -> s.writer2()));
        for (Future<?> f : res) {
            try {
                f.get();
            } catch (ExecutionException e) {
                throw e.getCause();
            }
        }
        try {
            pool.submit(() ->s.arbiter(r)).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
        counter.record(r);
    }

    private void sanityCheck_Footprints(Counter<ZZ_Result_jcstress> counter) throws Throwable {
        config.adjustStrides(size -> {
            version = new StateHolder<>(new BitSets[size], new ZZ_Result_jcstress[size], 2, config.spinLoopStyle);
            for (int c = 0; c < size; c++) {
                ZZ_Result_jcstress r = new ZZ_Result_jcstress();
                BitSets s = new BitSets();
                version.rs[c] = r;
                version.ss[c] = s;
                s.writer1();
                s.writer2();
                s.arbiter(r);
                counter.record(r);
            }
        });
    }

    @Override
    public Counter<ZZ_Result_jcstress> internalRun() {
        version = new StateHolder<>(new BitSets[0], new ZZ_Result_jcstress[0], 2, config.spinLoopStyle);

        control.isStopped = false;

        List<Callable<Counter<ZZ_Result_jcstress>>> tasks = new ArrayList<>();
        tasks.add(this::writer1);
        tasks.add(this::writer2);
        Collections.shuffle(tasks);

        Collection<Future<Counter<ZZ_Result_jcstress>>> results = new ArrayList<>();
        for (Callable<Counter<ZZ_Result_jcstress>> task : tasks) {
            results.add(pool.submit(task));
        }

        try {
            TimeUnit.MILLISECONDS.sleep(config.time);
        } catch (InterruptedException e) {
        }

        control.isStopped = true;

        waitFor(results);

        Counter<ZZ_Result_jcstress> counter = new Counter<>();
        for (Future<Counter<ZZ_Result_jcstress>> f : results) {
            try {
                counter.merge(f.get());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
        return counter;
    }

    public final void jcstress_consume(StateHolder<BitSets, ZZ_Result_jcstress> holder, Counter<ZZ_Result_jcstress> cnt, int a, int actors) {
        BitSets[] ss = holder.ss;
        ZZ_Result_jcstress[] rs = holder.rs;
        int len = ss.length;
        int left = a * len / actors;
        int right = (a + 1) * len / actors;
        for (int c = left; c < right; c++) {
            ZZ_Result_jcstress r = rs[c];
            BitSets s = ss[c];
            s.arbiter(r);
            ss[c] = new BitSets();
            cnt.record(r);
            r.r1 = false;
            r.r2 = false;
        }
    }

    public final void jcstress_updateHolder(StateHolder<BitSets, ZZ_Result_jcstress> holder) {
        if (!holder.tryStartUpdate()) return;
        BitSets[] ss = holder.ss;
        ZZ_Result_jcstress[] rs = holder.rs;
        int len = ss.length;

        int newLen = holder.updateStride ? Math.max(config.minStride, Math.min(len * 2, config.maxStride)) : len;

        BitSets[] newS = ss;
        ZZ_Result_jcstress[] newR = rs;
        if (newLen > len) {
            newS = Arrays.copyOf(ss, newLen);
            newR = Arrays.copyOf(rs, newLen);
            for (int c = len; c < newLen; c++) {
                newR[c] = new ZZ_Result_jcstress();
                newS[c] = new BitSets();
            }
         }

        version = new StateHolder<>(control.isStopped, newS, newR, 2, config.spinLoopStyle);
        holder.finishUpdate();
   }

    public final Counter<ZZ_Result_jcstress> writer1() {

        Counter<ZZ_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<BitSets,ZZ_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            BitSets[] ss = holder.ss;
            ZZ_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                BitSets s = ss[c];
                s.writer1();
            }

            holder.postRun();

            jcstress_consume(holder, counter, 0, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

    public final Counter<ZZ_Result_jcstress> writer2() {

        Counter<ZZ_Result_jcstress> counter = new Counter<>();
        while (true) {
            StateHolder<BitSets,ZZ_Result_jcstress> holder = version;
            if (holder.stopped) {
                return counter;
            }

            BitSets[] ss = holder.ss;
            ZZ_Result_jcstress[] rs = holder.rs;
            int size = ss.length;

            holder.preRun();

            for (int c = 0; c < size; c++) {
                BitSets s = ss[c];
                s.writer2();
            }

            holder.postRun();

            jcstress_consume(holder, counter, 1, 2);
            jcstress_updateHolder(holder);

            holder.postUpdate();
        }
    }

}
