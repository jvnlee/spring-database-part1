package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void transfer(String senderId, String receiverId, int money) throws SQLException {
        Connection con = dataSource.getConnection();

        try {
            con.setAutoCommit(false); // 트랜잭션 시작
            bizLogic(senderId, receiverId, money, con);
            con.commit(); // 정상 -> 커밋
        } catch (Exception e) {
            con.rollback(); // 예외 발생 -> 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }

    // 기존 transfer 메서드에 그대로 두게 되면, 트랜잭션을 위한 로직과 비즈니스 로직이 한 곳에 있게 되므로 코드 분리를 위해 메서드로 추출함
    private void bizLogic(String senderId, String receiverId, int money, Connection con) throws SQLException {
        Member sender = memberRepository.findById(senderId, con);
        Member receiver = memberRepository.findById(receiverId, con);

        memberRepository.update(senderId, sender.getMoney() - money, con);
        validation(receiver);
        memberRepository.update(receiverId, receiver.getMoney() + money, con);
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                // 커넥션 풀로 반환하는 경우, autocommit 설정을 기본값(true)으로 되돌리지 않으면 바뀐 그대로 남아있게 됨
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
