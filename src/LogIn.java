import java.sql.*;
import java.util.*;

public class LogIn {
    private Scanner sc = new Scanner(System.in);
    List<User> users = new ArrayList<User>();
    String isAdmin = "false";
    Integer userID = null;

    public void logInMenu(String url, String user, String password) {
        System.out.println("Enter your username");
        String inputUsername = sc.nextLine();

        System.out.println("Enter your password");
        String inputPassword = sc.nextLine();
        String passwordUser = "";


        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            String query = "SELECT * FROM users where user_name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, inputUsername);
            //System.out.println(inputUsername);
            ResultSet resultSet = preparedStatement.executeQuery();



            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String userName = resultSet.getString("user_name");
                passwordUser = resultSet.getString("passwordUser");
                users.add(new User(id, userName, passwordUser));
                //System.out.println(users);


            }
        }catch (SQLException e) {
            System.out.println("Try again");

        }

        if (!users.isEmpty()) {
            if (inputPassword.equals(passwordUser)) {
                userID = users.get(0).getId();
                System.out.println(userID);
                System.out.println("You are logged in as " + inputUsername);
                if (inputUsername.equals("admin")) {
                    isAdmin = "true";
                }
                new ConsoleMenu().showMenu(url, user, password, isAdmin, userID);

            } else  {
                System.out.println("incorrect password/username");
                logInMenu(url, user, password);
            }
        } else {
            System.out.println("incorrect password/username");
            logInMenu(url, user, password);
        }




    }
}
