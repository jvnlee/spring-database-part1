package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    public void transfer(String senderId, String receiverId, int money) throws SQLException {
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
