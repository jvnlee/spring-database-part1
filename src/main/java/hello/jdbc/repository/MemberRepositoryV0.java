package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DBConnectionUtil.getConnection();
            pstmt = con.prepareStatement(sql);

            // 작성한 쿼리문에서 values 부분에 들어갈 실제 값은 set~ 메서드로 지정함
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            // 완성된 쿼리를 가지고 요청을 보내 DB에 반영
            pstmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * PreparedStatement와 Connection 객체는 DB와의 연결을 위해 자원을 사용하는 것들이므로 사용이 끝나면 반드시 닫아줘야함.
     * 생성 순서의 역순으로 닫아줌.
     */
    private void close(Connection con, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Close Error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.error("Close Error", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Close Error", e);
            }
        }
    }

}
