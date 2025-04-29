package uk.gov.laa.gpfd.dao;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;
import uk.gov.laa.gpfd.exception.DatabaseReadException;
import uk.gov.laa.gpfd.exception.ReportIdNotFoundException;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@AllArgsConstructor
public class ReportViewsDao {
    private final JdbcTemplate writeJdbcTemplate;

    @NotNull
    public List<Map<String, Object>> callDataBase(String sqlQuery) throws ReportIdNotFoundException {
        List<Map<String, Object>> resultList;

        try {
            log.debug("Retrieving data");
            resultList = writeJdbcTemplate.queryForList(sqlQuery);
        } catch (DataAccessException e) {
            throw new DatabaseReadException("Error reading from DB: " + e);
        }

        if (resultList.isEmpty()) {
            throw new DatabaseReadException("No results returned from query to MOJFIN reports database");
        }

        log.info("returning result list");
        return resultList;
    }

    public List<Map<String, Object>> callDataBaseROB(String sqlQuery, OutputStream stream) {
        writeJdbcTemplate.query(sqlQuery, new RowCallbackHandler() {
            private boolean firstRow = true;

            protected Map<String, Object> createColumnMap(int columnCount) {
                return new LinkedCaseInsensitiveMap(columnCount);
            }

            @Override
            public void processRow(ResultSet rs) throws SQLException {
                try {
                    if (!firstRow) {
                        stream.write(",".getBytes()); // Separator between items
                    } else {
                        firstRow = false;
                    }

                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    Map<String, Object> mapOfColumnValues = this.createColumnMap(columnCount);

                    for(int i = 1; i <= columnCount; ++i) {
                        String column = JdbcUtils.lookupColumnName(rsmd, i);
                        mapOfColumnValues.putIfAbsent(this.getColumnKey(column), this.getColumnValue(rs, i));
                    }
                    // Convert row to JSON and write to output stream
//                    String jsonRow = convertRowToJson(rs);
                    stream.write("mapOfColumnValues".getBytes());
                    stream.flush();
                } catch (Exception e) {
                    throw new SQLException("Error streaming data", e);
                }
            }

            protected String getColumnKey(String columnName) {
                return columnName;
            }

            @Nullable
            protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
                return JdbcUtils.getResultSetValue(rs, index);
            }
        });
        return null;
    }




    private String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        var escaped = input.replace("\"", "\"\"");
        if (Set.of(",", "\n", "\"").stream().anyMatch(escaped::contains)) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String convertRowToJson(ResultSet rs) throws SQLException {
        return String.format("{\"id\":%d,\"name\":\"%s\"}",
                rs.getInt("id"),
                rs.getString("name"));
    }
}
