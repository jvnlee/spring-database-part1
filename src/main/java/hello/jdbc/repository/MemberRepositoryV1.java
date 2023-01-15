package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static org.springframework.jdbc.support.JdbcUtils.*;

/**
 * JDBC - DataSource와 JdbcUtils 사용
 */
@Slf4j
public class MemberRepositoryV1 {

    /**
     * 이제부터는 DataSource를 주입받아 사용하게 됨.
     * 매 요청마다 직접 커넥션을 생성해서 반환해주는 DriverManagerDataSource 같은 방식이나, 미리 일정 개수의 커넥션을 생성해두고 거기서 커넥션을 반환해주는 HikariDataSource 같은 방식 모두 DataSource 인터페이스의 구현체이기 때문에 어느 방식을 사용하던 클라이언트 코드는 더이상 변경할 필요가 없어짐.
     */
    private final DataSource dataSource;

    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

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
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));

                log.info("Found member={}", member);
                return member;
            } else {
                throw new NoSuchElementException("Member with member_id " + memberId + " does not exist.");
            }
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("DB Error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * JdbcUtils의 스태틱 메서드들을 활용해서 보다 간편하게 닫아줄 수 있음
     */
    private void close(Connection con, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(con);
    }

}
