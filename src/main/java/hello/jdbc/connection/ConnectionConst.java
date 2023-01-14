package hello.jdbc.connection;

public class ConnectionConst {

    // 커넥션을 위해 필요한 정보들을 상수화
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

    // 인스턴스 생성 방지
    private ConnectionConst() {
    }

}
