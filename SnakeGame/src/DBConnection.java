
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnection {
	private Connection con;
	private Statement st;
	ResultSet rs;

	// 데이터베이스 연결
	public DBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/snake_db?characterEncoding=UTF-8&serverTimezone=UTC", "root",
					"defacto8*");
			st = con.createStatement();
		} catch (Exception e) {
			System.out.println("데이터베이스 연결 오류 : " + e.getMessage());
		}
	}

	// 데이터베이스에 스코어 기록
	public void logScore(int score) {
		try {
			String SQL = "insert into score (score) values (" + score + ")";
			st.executeUpdate(SQL);
		} catch (Exception e) {
			System.out.println("데이터베이스 오류 : " + e.getMessage());
		}
	}

	// 최고기록 불러오기
	public int getBestScore() {
		int score = 0;
		try {
			String SQL = "select MAX(score) from score";
			rs = st.executeQuery(SQL);
			while (rs.next()) {
				score = rs.getInt("MAX(score)");
			}
		} catch (Exception e) {
			System.out.println("데이터베이스 오류 : " + e.getMessage());
		}
		return score;
	}
}
