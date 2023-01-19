package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    // @Transactional 어노테이션을 붙이면 스프링이 AOP를 통해 트랜잭션 처리 관련 코드를 비즈니스 로직 앞뒤로 붙여줌
    @Transactional
    public void transfer(String senderId, String receiverId, int money) throws SQLException {
        bizLogic(senderId, receiverId, money);
    }

    private void bizLogic(String senderId, String receiverId, int money) throws SQLException {
        Member sender = memberRepository.findById(senderId);
        Member receiver = memberRepository.findById(receiverId);

        memberRepository.update(senderId, sender.getMoney() - money);
        validation(receiver);
        memberRepository.update(receiverId, receiver.getMoney() + money);
    }

    private void validation(Member receiver) {
        if (receiver.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 작업 도중 예외 발생!");
        }
    }

}
