package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate transactionTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    public void transfer(String senderId, String receiverId, int money) {
        transactionTemplate.executeWithoutResult((status) -> {
            // 람다식에서는 checked 예외를 밖으로 던져줄 방법이 없어서 부득이하게 try-catch 블럭이 포함됨
            try {
                bizLogic(senderId, receiverId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private void bizLogic(String senderId, String receiverId, int money) throws SQLException {
        Member sender = memberRepository.findById(senderId);
        Member receiver = memberRepository.findById(receiverId);

        memberRepository.update(senderId, sender.getMoney() - money);
        validation(receiver);
        memberRepository.update(receiverId, receiver.getMoney() + money);
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (Exception e) {
                log.error("error", e);
            }
        }
    }

    private void validation(Member receiver) {
        if (receiver.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체 작업 도중 예외 발생!");
        }
    }

}
