package cn.cicoding.sharding.algorithm;

import lombok.SneakyThrows;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 日期分片算法
 *
 * @author oakhole
 * @version 1.0
 * @since 2021/4/15
 */
public final class DateTimeRangeShardingAlgorithm implements PreciseShardingAlgorithm, RangeShardingAlgorithm {

    @SneakyThrows
    @Override
    public String doSharding(final Collection availableTargetNames, final PreciseShardingValue shardingValue) {
        return (String) availableTargetNames.stream()
                .filter(each -> ((String) each).endsWith(parseDateTime(shardingValue.getValue().toString()).format(DateTimeFormatter.ofPattern("yyyyMM"))))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException(
                                String.format("failed to shard value %s, and availableTables %s",
                                        shardingValue,
                                        availableTargetNames)
                        )
                );
    }

    @Override
    public Collection<String> doSharding(final Collection availableTargetNames, final RangeShardingValue shardingValue) {
        boolean hasStartTime = shardingValue.getValueRange().hasLowerBound();
        boolean hasEndTime = shardingValue.getValueRange().hasUpperBound();
        if (!hasStartTime && !hasEndTime) {
            return availableTargetNames;
        }

        // 默认时间范围为6月前至今
        LocalDateTime dateTimeUpper = LocalDateTime.now();
        LocalDateTime dateTimeLower = dateTimeUpper.minusMonths(6);

        LocalDateTime startTime = hasStartTime ? parseDateTime(shardingValue.getValueRange().lowerEndpoint().toString()) : dateTimeLower;
        LocalDateTime endTime = hasEndTime ? parseDateTime(shardingValue.getValueRange().upperEndpoint().toString()) : dateTimeUpper;
        LocalDateTime calculateTime = startTime;
        Set<String> result = new HashSet<>();
        while (!calculateTime.isAfter(endTime)) {
            result.addAll(getMatchedTables(calculateTime, availableTargetNames));
            calculateTime = calculateTime.plus(1, ChronoUnit.MONTHS);
        }
        result.addAll(getMatchedTables(endTime, availableTargetNames));
        return result;
    }

    /**
     * 日期解析
     *
     * @param value 日期时间
     * @return
     */
    private LocalDateTime parseDateTime(final String value) {
        return LocalDateTime.parse(value.substring(0, 19), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 匹配需要查询的表
     *
     * @param dateTime
     * @param availableTargetNames
     * @return
     */
    private Collection<String> getMatchedTables(final LocalDateTime dateTime, final Collection<String> availableTargetNames) {
        String tableSuffix = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        return availableTargetNames.parallelStream().filter(each -> each.endsWith(tableSuffix)).collect(Collectors.toSet());
    }

}
