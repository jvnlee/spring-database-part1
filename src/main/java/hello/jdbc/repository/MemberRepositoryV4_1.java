package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.exception.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static org.springframework.jdbc.support.JdbcUtils.closeResultSet;
import static org.springframework.jdbc.support.JdbcUtils.closeStatement;

/**
 * TransactionManager
 */
@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository {

    private final DataSource dataSource;

    public MemberRepositoryV4_1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DataSourceUtils.getConnection(dataSource);
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());

            pstmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            /*
            Checked Exception인 SQLException을 Unchecked Exception인 MyDbException으로 전환해서 던짐

            효과:
            - 해당 메서드를 호출하는 클라이언트 코드가 더 이상 JDBC 기술의 일부인 SQLException에 의존적이지 않아도 됨
            (런타임 예외는 선언부에 명시할 의무가 없기 때문)
            - 해당 메서드의 선언부에서 예외 선언을 제거할 수 있음. 따라서 이 클래스를 인터페이스로 추상화할 때도 메서드 선언부가 깨끗해지고, SQLException에 의존적이지 않게 됨
             */
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DataSourceUtils.getConnection(dataSource);
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
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DataSourceUtils.getConnection(dataSource);
            pstmt = con.prepareStatement(sql);

            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = DataSourceUtils.getConnection(dataSource);
            pstmt = con.prepareStatement(sql);

            pstmt.setString(1, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new MyDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);

        /*
         기존의 JdbcUtils가 아닌 DataSourceUtils를 통해 커넥션을 닫아주어야함
         con.close() 해버리면 말 그대로 해당 커넥션을 완전히 종료시켜버리는 것인데, 동기화된 커넥션인 경우 트랜잭션 commit 혹은 rollback 시까지 계속 유지되어야하기 때문
         DataSourceUtils.releaseConnection()은 해당 커넥션이 트랜잭션을 위한 동기화된 커넥션이면 살려두고, 그렇지 않은 경우에는 닫음.
         */
        DataSourceUtils.releaseConnection(con, dataSource);
    }

}
