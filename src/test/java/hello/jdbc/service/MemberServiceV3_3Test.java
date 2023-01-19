package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// 테스트 시 스프링 컨테이너를 생성하고, 컨테이너에 등록된 빈들을 DI 받아 사용할 수 있음
@SpringBootTest
@Slf4j
class MemberServiceV3_3Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberRepositoryV3 memberRepository;

    // 클래스 내부에 @Transactional 메서드가 존재하기 때문에, 실제 MemberService 객체가 아닌 스프링 AOP가 생성한 프록시 객체를 주입받게 됨 (로그 찍어보면 알 수 있음)
    @Autowired
    private MemberServiceV3_3 memberService;

    // 스프링 부트에서 기본적으로 등록해주는 빈 외에도 추가적으로 필요한 빈들을 컨테이너에 올리고 사용할 수 있음
    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        // 스프링의 트랜잭션 AOP는 빈으로 등록된 트랜잭션 매니저를 사용하기 때문에 매니저를 빈으로 등록해두어야 함
        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }

    @AfterEach
    void afterEach() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("AOP 프록시 객체 확인")
    void aopTest() {
        // hello.jdbc.service.MemberServiceV3_3$$EnhancerBySpringCGLIB$$34be84f1
        log.info("memberService={}", memberService.getClass());
        // isAopProxy(): 파라미터로 넘긴 객체가 AOP 객체인지 검증하는 메서드
        assertThat(AopUtils.isAopProxy(memberService)).isTrue();
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