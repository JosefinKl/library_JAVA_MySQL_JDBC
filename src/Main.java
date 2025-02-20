import java.sql.*;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/library";
        String user = "root";
        String password = System.getenv("MYSQL_PASSWORD");

        new LogIn.logInMenu(url, user, password);
        new ConsoleMenu().showMenu(url, user, password);


        }
        ;
    }
