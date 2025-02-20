import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class ConsoleMenu {
    private Scanner sc = new Scanner(System.in);
    ArrayList<Integer> availableID = new ArrayList<>(Arrays.asList());

    public void showMenu (String url, String user, String password){
        System.out.println("----Meny----");
        System.out.println("1. Se alla böcker");
        System.out.println("0. Avsluta");

        String choice = sc.nextLine();
        switch (choice) {
            case "1":
                showBooks(url, user, password);
                loanBook();
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid choice, try again");
                showMenu(url, user, password);

        }
    }
    public void loanBook(){
        System.out.println("Do you want to loan one of the books, insert the ID-number (to quit, write QUIT)?");
        String choice = sc.next();
        boolean bookAvailable = false;
        if (choice.equals("QUIT")) {
            return;
        }
        try {
            int choiceID = Integer.parseInt(choice);
            for (int element : availableID) {
                if (element == choiceID) {
                    bookAvailable = true;
                    break;
                }
            }
            if (bookAvailable) {
                System.out.println("The book is available");


            } else {
                System.out.println("The book is not available/Invalid choice");
                loanBook();
            }
        }catch (Exception e){
            System.out.println("Invalid choice, try again");
            loanBook();
        }


    }

    private void showBooks(String url, String user, String password){
        try {

            Connection connection = DriverManager.getConnection(url, user, password);
            String query = "SELECT * FROM books";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();




            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String available = resultSet.getString("available");
                if (Objects.equals(available, "available")){
                    availableID.add(id);
                    //System.out.println(availableID);
                }

                System.out.println(id + " : " + title + " : " + author + " : " + available);
            }
        } catch (SQLException e) {
            System.out.println("Databasanslutning misslyckad");
        };
    }


}
