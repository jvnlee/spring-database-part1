package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection() {
        try {
            /*
            DriverManager가 라이브러리 목록에서 적합한 DB Driver를 찾아서 커넥션 생성 시도
            Connection 인터페이스를 구현하고 있는 org.h2.jdbc.JdbcConnection 객체를 반환함
             */
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("Get Connection={}, Class={}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e); // checked exception을 unchecked exception으로 전환
        }
    }

}
