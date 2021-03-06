package com.rudyii.hsw.providers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.rudyii.hsw.configuration.OptionsService;
import com.rudyii.hsw.database.FirebaseDatabaseProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.rudyii.hsw.configuration.OptionsService.KEEP_DAYS;
import static com.rudyii.hsw.providers.StatsProvider.Action.CLEANUP;
import static com.rudyii.hsw.providers.StatsProvider.Action.INCREASE;
import static java.math.BigInteger.ZERO;

@Slf4j
@Component
public class StatsProvider {
    private final FirebaseDatabaseProvider firebaseDatabaseProvider;
    private final OptionsService optionsService;

    @Autowired
    public StatsProvider(FirebaseDatabaseProvider firebaseDatabaseProvider, OptionsService optionsService) {
        this.firebaseDatabaseProvider = firebaseDatabaseProvider;
        this.optionsService = optionsService;
    }

    public void increaseArmedStatistic() {
        firebaseDatabaseProvider.getReference("/usageStats").addListenerForSingleValueEvent(getUsageStatsValueEventListener(INCREASE));
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupObsolete() {
        firebaseDatabaseProvider.getReference("/usageStats").addListenerForSingleValueEvent(getUsageStatsValueEventListener(CLEANUP));
    }

    private ValueEventListener getUsageStatsValueEventListener(Action action) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ConcurrentHashMap<String, Long> usageStats;
                String today = getToday();
                HashMap<String, Long> data = (HashMap<String, Long>) dataSnapshot.getValue();

                if (data == null) {
                    usageStats = new ConcurrentHashMap<>();
                } else {
                    usageStats = new ConcurrentHashMap<>(data);
                }

                if (usageStats.get(today) == null) {
                    log.info("Initialized today with ZERO");
                    usageStats.put(today, ZERO.longValue());
                }

                switch (action) {
                    case CLEANUP:
                        Long historicalToday = calculateHistoricalToday();
                        log.info("Cleaning obsolete usage stats");
                        usageStats.forEach((k, v) -> {
                            if (Long.parseLong(k) < historicalToday) {
                                usageStats.remove(k);
                            }
                        });
                        break;
                    case INCREASE:
                        Long armedTodayCount = usageStats.get(today);
                        usageStats.put(today, armedTodayCount + 1);
                        break;
                }

                firebaseDatabaseProvider.pushData("/usageStats", usageStats);
            }

            @NotNull
            private String getToday() {
                return new SimpleDateFormat("yyyyMMdd").format(new Date());
            }

            private Long calculateHistoricalToday() {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());

                calendar.add(Calendar.DATE, -((Long) optionsService.getOption(KEEP_DAYS)).intValue());

                return Long.valueOf(new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                log.error("Failed to fetch Weekly stats Firebase data!");
            }
        };
    }

    enum Action {INCREASE, CLEANUP}
}
