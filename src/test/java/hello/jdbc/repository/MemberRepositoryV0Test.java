package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MemberRepositoryV0Test {

    MemberRepositoryV0 repository = new MemberRepositoryV0();
    Member member = new Member("memberV0", 10000);

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