package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void transfer(String senderId, String receiverId, int money) throws SQLException {
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            bizLogic(senderId, receiverId, money);
            transactionManager.commit(status); // 정상 -> 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); // 예외 발생 -> 롤백
            throw new IllegalStateException(e);
        }

        /*
         커넥션 release는 더 이상 직접 해주지 않아도 됨.
         TransactionManager가 커밋 혹은 롤백 후에 트랜잭션 수행이 끝났음을 알고서 알아서 해주기 때문
         */
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
