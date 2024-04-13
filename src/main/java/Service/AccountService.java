package Service;

import DAO.AccountDAO;
import Model.Account;
import java.sql.SQLException;

public class AccountService {
    private final AccountDAO accountDAO;

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    // Parameterless constructor
    public AccountService() {
        this.accountDAO = new AccountDAO(); // Assumes AccountDAO has a no-arg constructor
    }
    
    /**
     * Registers a new account with the provided username and password.
     * Before creating an account, this method checks if the username is already taken.
     *
     * @param account An Account object containing the username and password.
     * @return The created Account object with an assigned account_id, or null if the account cannot be created.
     * @throws Exception If the username is already taken or if any other issue occurs during account creation.
     */
    public Account registerAccount(Account account) throws Exception {
        // Validate the account details
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (account.getPassword() == null || account.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        
        // Check if the username is already taken
        Account existingAccount = accountDAO.getAccountByUsername(account.getUsername());
        if (existingAccount != null) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        
        // Create the account
        return accountDAO.createAccount(account);
    }

    /**
     * Verifies if the login credentials are correct.
     *
     * @param username The username.
     * @param password The password.
     * @return The Account object if credentials are correct; null otherwise.
     * @throws SQLException If a database access error occurs.
     */
    public Account login(String username, String password) throws SQLException {
        Account account = accountDAO.getAccountByUsername(username);
        
        if (account != null && account.getPassword().equals(password)) {
            return account;
        }
        
        return null;
    }

    public boolean exists(int userId) {
        try {
            // Attempt to retrieve the user by ID from the database using AccountDAO
            return accountDAO.getAccountById(userId) != null;
        } catch (SQLException e) {
            // Log the exception
            e.printStackTrace();
            // Return false or handle it based on your application's requirements
            return false;
        }
    }
    
    
}
