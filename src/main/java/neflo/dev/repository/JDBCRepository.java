package neflo.dev.repository;

import lombok.RequiredArgsConstructor;
import neflo.dev.exceptions.DatabaseException;
import neflo.dev.model.dto.YearMonth;
import neflo.dev.model.dto.group.GroupInsights;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JDBCRepository {

    private final JdbcTemplate db;

    private static final String INSIGHTS_MONTHLY_QUERY = """
            SELECT EXTRACT('YEAR' FROM T.DATE) AS YEAR, EXTRACT('MONTH' FROM T.DATE) AS MONTH, SUM(T.DURATION_MINUTES) AS DRIVING_TIME, D.NICKNAME AS NICKNAME
            FROM GRP_TRIPS T
            JOIN GRP_GROUPS G ON T.GRP_ID = G.ID
            JOIN USR_USERS D ON T.DRIVER = D.ID
            WHERE G.ID = ? AND DATE_TRUNC('MONTH', T.DATE) = ?
            GROUP BY EXTRACT('YEAR' FROM T.DATE), EXTRACT('MONTH' FROM T.DATE), D.NICKNAME;
            """;

    private static final String INSIGHTS_YEARLY_QUERY = """
            SELECT EXTRACT('YEAR' FROM T.DATE) AS YEAR, SUM(T.DURATION_MINUTES) AS DRIVING_TIME, D.NICKNAME AS NICKNAME
            FROM GRP_TRIPS T
            JOIN GRP_GROUPS G ON T.GRP_ID = G.ID
            JOIN USR_USERS D ON T.DRIVER = D.ID
            WHERE G.ID = ? AND DATE_TRUNC('YEAR', T.DATE) = ?
            GROUP BY EXTRACT('YEAR' FROM T.DATE), D.NICKNAME;
            """;

    private static final ResultSetExtractor<GroupInsights> MONTHLY_INSIGHTS_RSE = rs -> {
        if (!rs.next()) {
            throw new DatabaseException("no-result-request", "Database query returned no results.");
        }
        int year = rs.getInt("YEAR");
        Integer month = rs.getInt("MONTH");
        month = month == 0 ? null : month;

        Map<String, Integer> driverTimes = new HashMap<>();

        do {
            String nickName = rs.getString("NICKNAME");
            int drivingTime = rs.getInt("DRIVING_TIME");
            driverTimes.put(nickName, drivingTime);
        } while (rs.next());

        return new GroupInsights(
                year,
                month,
                driverTimes
        );
    };

    private static final ResultSetExtractor<GroupInsights> YEARLY_INSIGHTS_RSE = rs -> {
        if (!rs.next()) {
            throw new DatabaseException("no-result-request", "Database query returned no results.");
        }
        int year = rs.getInt("YEAR");

        Map<String, Integer> driverTimes = new HashMap<>();

        do {
            String nickName = rs.getString("NICKNAME");
            int drivingTime = rs.getInt("DRIVING_TIME");
            driverTimes.put(nickName, drivingTime);
        } while (rs.next());

        return new GroupInsights(
                year,
                null,
                driverTimes
        );
    };

    public GroupInsights getMonthlyGroupInsights(UUID groupId, YearMonth yearMonth) {
        try {
            Object[] params = new Object[2];
            params[0] = groupId;
            params[1] = LocalDate.of(yearMonth.year(), yearMonth.month(), 1);

            return db.query(INSIGHTS_MONTHLY_QUERY, MONTHLY_INSIGHTS_RSE, params);
        } catch (Exception e){
            throw new DatabaseException("monthly-insights-error", "There was an error trying to retrieve requested's month group's insights.", e);
        }
    }

    public GroupInsights getYearlyGroupInsights(UUID groupId, int year) {
        try {
            Object[] params = new Object[2];
            params[0] = groupId;
            params[1] = LocalDate.of(year, 1, 1);

            return db.query(INSIGHTS_YEARLY_QUERY, YEARLY_INSIGHTS_RSE, params);
        } catch (Exception e){
            throw new DatabaseException("yearly-insights-error", "There was an error trying to retrieve requested's year group's insights.", e);
        }
    }

}
