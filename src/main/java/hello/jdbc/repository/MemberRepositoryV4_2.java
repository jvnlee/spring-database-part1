package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.exception.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static org.springframework.jdbc.support.JdbcUtils.closeResultSet;
import static org.springframework.jdbc.support.JdbcUtils.closeStatement;

/**
 * TransactionManager
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    // 스프링이 제공하는 SQLException 예외 변환기
    private final SQLExceptionTranslator exceptionTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
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
            // 우리가 직접 구체적인 커스텀 예외를 생성해서 전환시켜주지 않아도 translator가 적절한 예외로 전환해서 던져줌. (ex. DuplicateKeyException)
            throw exceptionTranslator.translate("save", sql, e);
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
            throw exceptionTranslator.translate("findById", sql, e);
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
            throw exceptionTranslator.translate("update", sql, e);
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
            throw exceptionTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        closeResultSet(rs);
        closeStatement(stmt);
        DataSourceUtils.releaseConnection(con, dataSource);
    }

}
