package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MemberServiceV3_2Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_2 memberService;

    @BeforeEach
    void beforeEach() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_2(new DataSourceTransactionManager(dataSource), memberRepository);
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void transfer() throws SQLException {
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberB);

        memberService.transfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        Member foundMemA = memberRepository.findById(memberA.getMemberId());
        Member foundMemB = memberRepository.findById(memberB.getMemberId());

        assertThat(foundMemA.getMoney()).isEqualTo(8000);
        assertThat(foundMemB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void transferEx() throws SQLException {
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        assertThrows(IllegalStateException.class, () -> memberService.transfer(memberA.getMemberId(), memberEx.getMemberId(), 2000));

        Member foundMemA = memberRepository.findById(memberA.getMemberId());
        Member foundMemEx = memberRepository.findById(memberEx.getMemberId());

        // 예외 발생으로 인해 롤백되었기 때문에 둘다 잔고 10000원인 초기 상태로 되돌아감
        assertThat(foundMemA.getMoney()).isEqualTo(10000);
        assertThat(foundMemEx.getMoney()).isEqualTo(10000);
    }
}