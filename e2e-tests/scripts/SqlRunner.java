import java.sql.*;

/**
 * Minimal JDBC helper to inspect / seed the remote Open WES database.
 * Usage: java SqlRunner <sql>
 *   - A single statement is executed; SELECT results are printed as TSV.
 *   - Example: java SqlRunner "SHOW TABLES"
 *   - Multi-statement mode: prefix the arg with ";" as the first char.
 */
public class SqlRunner {
    public static void main(String[] args) throws Exception {
        String sql = args[0];
        String url = "jdbc:mysql://192.168.127.129:3306/openwes?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        try (Connection conn = DriverManager.getConnection(url, "root", "root")) {
            if (sql.startsWith(";")) {
                // multi-statement run
                try (Statement st = conn.createStatement()) {
                    for (String s : sql.substring(1).split(";;")) {
                        if (s.trim().isEmpty()) continue;
                        boolean hasResult = st.execute(s);
                        if (hasResult) {
                            try (ResultSet rs = st.getResultSet()) {
                                print(rs);
                            }
                        } else {
                            System.out.println("OK rows=" + st.getUpdateCount());
                        }
                    }
                }
                return;
            }
            if (sql.trim().toLowerCase().startsWith("select") || sql.trim().toLowerCase().startsWith("show")
                    || sql.trim().toLowerCase().startsWith("desc")) {
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    print(rs);
                }
            } else {
                try (Statement st = conn.createStatement()) {
                    int n = st.executeUpdate(sql);
                    System.out.println("OK rows=" + n);
                }
            }
        }
    }

    static void print(ResultSet rs) throws Exception {
        ResultSetMetaData md = rs.getMetaData();
        int cols = md.getColumnCount();
        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= cols; i++) {
            if (i > 1) header.append("\t");
            header.append(md.getColumnLabel(i));
        }
        System.out.println(header);
        int rows = 0;
        while (rs.next()) {
            StringBuilder line = new StringBuilder();
            for (int i = 1; i <= cols; i++) {
                if (i > 1) line.append("\t");
                line.append(rs.getString(i));
            }
            System.out.println(line);
            rows++;
            if (rows > 200) { System.out.println("...truncated"); break; }
        }
        System.out.println("(" + rows + " rows)");
    }
}
