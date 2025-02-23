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
    ArrayList<Integer> allBookIDinList = new ArrayList<>(Arrays.asList());

    public void showMenu (String url, String user, String password, String isAdmin, Integer userID){
        System.out.println("----Menu----");
        System.out.println("1. All books");
        if(isAdmin == "false") {
            System.out.println("2. Loan a book");
            System.out.println("3. Return a book");
            System.out.println("4. Current loans");
        } else if (isAdmin == "true"){
            System.out.println("5. Add book");
            System.out.println("6. Remove book");

        }
        System.out.println("0. Avsluta");

        int choice;
        try {
        choice = sc.nextInt();
        }
        catch (Exception e) {
            System.out.println("Invalid choice");
            sc.nextLine();  //read the whole line so empty the scanner.
            showMenu(url, user, password, isAdmin, userID);
            return;
        }


        switch (choice) {
            case 1:
                showBooks(url, user, password);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case 2:
                showBooks(url, user, password);
                loanBook(url, user, password, userID, isAdmin);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case 3:
                unloanBook(url, user, password, userID, isAdmin);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case 4:
                //Current loans
                seeLoanedBooks(url, user, password, userID);
                showMenu(url, user, password, isAdmin, userID);

                break;
            case 5:
                addBook(url, user, password);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case 6:

                deleteBook(url, user, password);
                showMenu(url, user, password, isAdmin, userID);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice, try again");
                showMenu(url, user, password, isAdmin, userID);

        }
    }

    //function to loan a book
    public void loanBook(String url, String user, String password, Integer userID, String isAdmin){
        System.out.println("Do you want to loan one of the books, insert the ID-number of the book (to quit, write QUIT)?");
        String choice = sc.next();

        boolean bookAvailable = false; //used to check if book is available
        if (choice.equals("QUIT")) {
            return;
        }

        //to catch if invalid input and SQL error
        try {
            int choiceID = Integer.parseInt(choice);

            //check if book is available
            for (int element : availableID) {
                if (element == choiceID) {
                    bookAvailable = true;
                    break;
                }
            }

            //if the book is available loan it
            if (bookAvailable) {
                Date currentDate = new Date();  //today's date will be used as loan date
                java.sql.Date sqlDate = new java.sql.Date(currentDate.getTime());
                LocalDate localDate = sqlDate.toLocalDate();
                LocalDate newDate = localDate.plusDays(30);  //the loan time is 30 days
                java.sql.Date newSqlDate = java.sql.Date.valueOf(newDate);



                Connection connection = DriverManager.getConnection(url, user, password);
                String query = "insert into loans (user_id, book_id, loan_date, return_date) values (?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, userID);
                preparedStatement.setInt(2, choiceID);
                preparedStatement.setDate(3, sqlDate);
                preparedStatement.setDate(4, newSqlDate);

                Integer int1 = preparedStatement.executeUpdate();

                //Set the loaned book as unavailable
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

    //function to unloan a book
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

                    //Delete book from loan table
                    try {
                        Connection connection = DriverManager.getConnection(url, user, password);
                        String query = "DELETE FROM loans WHERE book_id = ?";

                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setInt(1, choiceID);
                        Integer int1 = preparedStatement.executeUpdate();

                        //Set book as available again
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

    //To see all loaned books for the current user
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

    //Administrator can add new books
    private void addBook (String url, String user, String password){
        sc.nextLine(); //read all to empty scanner.
        System.out.println("Enter book title: ");
        String title = sc.nextLine();
        System.out.println("Enter book author: ");
        String author = sc.nextLine();
        System.out.println("Book to add: " + title + " : " + author + ". Do you want to proceed to add the book (Y) or quit (QUIT)?" );
        String choice = sc.next();
        if (choice.equals("QUIT")) {
            return;
        }else if (choice.equals("Y")) {
            //Add new books with status available
            try{
                Connection connection = DriverManager.getConnection(url, user, password);
                String query = "insert into books (title, author, available) values (?, ?, 'available')";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, title);
                preparedStatement.setString(2, author);

                Integer int1 = preparedStatement.executeUpdate();
                System.out.println("The book is added");

            }catch(SQLException e) {
                System.out.println("Data connection error");
            };
        }
    }

    //Administrator can delete books
    private void deleteBook(String url, String user, String password) {
        showBooks(url, user, password);
        System.out.println("Which book to delete, insert the ID-number of the book. To quit write QUIT");
        String choice = sc.next();
        boolean bookPartOfList = false;
        if (choice.equals("QUIT")) {
            return;
        }
        try {
            int choiceID = Integer.parseInt(choice);

            for (int element : allBookIDinList) {
                if (element == choiceID) {
                    bookPartOfList = true;
                    break;
                }
            }
            if (bookPartOfList) {

                //Remove book from loans if it is loaned at the time for deletion (for example if the loaner lost the book)
                try{
                    Connection connection = DriverManager.getConnection(url, user, password);
                    String query = "DELETE FROM loans WHERE book_id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, choiceID);
                    Integer int1 = preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("Data connection error");
                }

                //Remove book from books list
                try {
                    Connection connection = DriverManager.getConnection(url, user, password);
                    String query = "delete from books where id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setInt(1, choiceID);


                    Integer int1 = preparedStatement.executeUpdate();

                } catch (SQLException e) {
                    System.out.println("Data connection error");
                }
                ;
            }

        } catch (Exception e) {
            System.out.println("Invalid choice, try again");
        }
        ;
    }

    //Show all books and their statuses
    private void showBooks(String url, String user, String password){
        availableID.clear();
        allBookIDinList.clear();
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
                allBookIDinList.add(id);
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
