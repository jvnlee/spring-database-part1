package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;
    Member member = new Member("memberV0", 10000);

    @BeforeEach
    void beforeEach() {
        // DriverManagerDataSource 활용 - 항상 새로운 커넥션 획득
        // DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // HikariDataSource 활용 - 커넥션 풀 안에 일정 개수의 커넥션을 미리 생성해두고 필요할 때 마다 여기서 획득
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void save() throws SQLException {
        repository.save(member);
    }

    @Test
    void findById() throws SQLException {
        Member foundMember = repository.findById(member.getMemberId());
        assertThat(foundMember).isEqualTo(member);
    }

    @Test
    void update() throws SQLException {
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20000);
    }

    @Test
    void delete() throws SQLException {
        repository.delete(member.getMemberId());
        assertThrows(NoSuchElementException.class, () -> repository.findById("memberV0"));
    }

}