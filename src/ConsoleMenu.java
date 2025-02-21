import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.util.Date;
import java.time.LocalDate;

public class ConsoleMenu {
    private Scanner sc = new Scanner(System.in);
    ArrayList<Integer> availableID = new ArrayList<>(Arrays.asList());
    ArrayList<Integer> loanedID = new ArrayList<>(Arrays.asList());

    public void showMenu (String url, String user, String password, String isAdmin, Integer userID){
        System.out.println("----Menu----");
        System.out.println("1. All books");
        System.out.println("2. Loan a book");
        System.out.println("3. Return a book");
        System.out.println("4. Current loans");
        if (isAdmin == "true"){
            System.out.println("5. Add book");
            System.out.println("6. Remove book");
            System.out.println("7. List all books");
        }
        System.out.println("0. Avsluta");

        String choice = sc.nextLine();
        switch (choice) {
            case "1":
                showBooks(url, user, password);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case "2":
                showBooks(url, user, password);
                loanBook(url, user, password, userID, isAdmin);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case "3":
                unloanBook(url, user, password, userID, isAdmin);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case "4":
                //Current loans
                seeLoanedBooks(url, user, password, userID);
                showMenu(url, user, password, isAdmin, userID);

                break;
            case "5":
                //add a book
                showMenu(url, user, password, isAdmin, userID);
                break;
            case "6":
                //remove book
                showMenu(url, user, password, isAdmin, userID);
                System.out.println("case 6");
                break;
            case "7":
                //all books
                showMenu(url, user, password, isAdmin, userID);
                break;
            case "0":
                return;
            default:
                System.out.println("Invalid choice, try again");
                showMenu(url, user, password, isAdmin, userID);

        }
    }
    public void loanBook(String url, String user, String password, Integer userID, String isAdmin){
        System.out.println("Do you want to loan one of the books, insert the ID-number of the book (to quit, write QUIT)?");
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
                Date currentDate = new Date();
                java.sql.Date sqlDate = new java.sql.Date(currentDate.getTime());
                LocalDate localDate = sqlDate.toLocalDate();
                LocalDate newDate = localDate.plusDays(30);
                java.sql.Date newSqlDate = java.sql.Date.valueOf(newDate);



                Connection connection = DriverManager.getConnection(url, user, password);
                String query = "insert into loans (user_id, book_id, loan_date, return_date) values (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, choiceID);
                preparedStatement.setDate(3, sqlDate);
                preparedStatement.setDate(4, newSqlDate);

                Integer int1 = preparedStatement.executeUpdate();

                try {
                    Connection connection2 = DriverManager.getConnection(url, user, password);
                    String query2 = "update books set available = 'unavailable' where id = ?";
                    PreparedStatement preparedStatement2 = connection2.prepareStatement(query2);
                    preparedStatement2.setInt(1, choiceID);
                    Integer int2 = preparedStatement2.executeUpdate();
                    System.out.println("The book is loaned");

                }catch (SQLException e) {
                    System.out.println("Data connection error");
                };



            } else {
                System.out.println("The book is not available/Invalid choice");
                loanBook(url, user, password, userID, isAdmin);
            }
        }catch (Exception e){
            System.out.println("Invalid choice, try again");
            loanBook(url, user, password, userID, isAdmin);
        }



    }

    public void unloanBook(String url, String user, String password, Integer userID, String isAdmin) {
        System.out.println("Your loans are:");
        seeLoanedBooks(url, user, password, userID);
        System.out.println("Which book (write ID number) to return? (to quit, write QUIT) ");
        String choice = sc.next();

        if (choice.equals("QUIT")) {
            return;
        }
        try {
            int choiceID = Integer.parseInt(choice);


            for (int element : loanedID) {
                if (element == choiceID) {
                    try {
                        Connection connection = DriverManager.getConnection(url, user, password);
                        String query = "DELETE FROM loans WHERE book_id = ?";

                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, choiceID);
                        Integer int1 = preparedStatement.executeUpdate();

                        try {
                            Connection connection2 = DriverManager.getConnection(url, user, password);
                            String query2 = "update books set available = 'available' where id = ?";
                            PreparedStatement preparedStatement2 = connection2.prepareStatement(query2);
                            preparedStatement2.setInt(1, choiceID);
                            Integer int2 = preparedStatement2.executeUpdate();
                            System.out.println("The book is returned");

                        }catch (SQLException e) {
                            System.out.println("Data connection error");
                        };



                    } catch (SQLException e) {
                        System.out.println("Data connection error");
                    };
                    break;
                }
            }
        }catch (Exception e){
            System.out.println("Invalid choice, try again");
        }
    }
    public void seeLoanedBooks(String url, String user, String password, Integer userID) {
        loanedID.clear();
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            String query = "SELECT books.id, books.title, books.author, loans.loan_date, loans.return_date FROM books INNER JOIN loans ON books.id=loans.book_id where user_id=?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userID);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Integer bookID = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String loan_date = resultSet.getString("loan_date");
                String return_date = resultSet.getString("return_date");
                loanedID.add(bookID);


                System.out.println(bookID + " " + title + " : " + author + " " + loan_date + " : " + return_date);
            }

        } catch(SQLException e) {
            System.out.println("Data connection error");
        };
    }
    public void returnBook(String url, String user, String password, Integer userID) {

    }

    private void showBooks(String url, String user, String password){
        availableID.clear();
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
            System.out.println("Data connection error");
        };
    }


}
