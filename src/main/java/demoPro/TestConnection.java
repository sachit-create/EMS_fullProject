package demoPro;

import java.sql.Connection;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection con = DataBaseConnection.getDefaultConnection()) {
            System.out.println("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
