package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

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

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DBConnectionUtil.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);

            // executeUpdate()는 데이터 수정 시에 사용함. 조회 시에는 executeQuery() 사용
            rs = pstmt.executeQuery();

            // ResultSet 내부의 커서를 반드시 한 번 이동시켜야 실제 유효한 데이터를 읽을 수 있음
            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                log.info("Found member={}", member);
                return member;
            } else {
                throw new NoSuchElementException("Member with member_id " + memberId + "does not exist.");
            }
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
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
