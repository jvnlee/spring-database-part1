package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class MemberServiceV4 {

    private final MemberRepository memberRepository;

    @Transactional
    public void transfer(String senderId, String receiverId, int money) {
        bizLogic(senderId, receiverId, money);
    }

    private void bizLogic(String senderId, String receiverId, int money) {
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
