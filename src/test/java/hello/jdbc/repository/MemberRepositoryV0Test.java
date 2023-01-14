package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

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
}