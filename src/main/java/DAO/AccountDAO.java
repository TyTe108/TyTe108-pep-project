package DAO;

import Model.Account;
import Util.ConnectionUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {
    /**
     * Inserts a new Account into the database.
     * 
     * @param account The account to be created, without an account_id.
     * @return The created Account with the account_id set.
     * @throws SQLException if a database access error occurs or the username is already taken.
     */
    public Account createAccount(Account account) throws SQLException {
        // SQL command for inserting a new account. The database auto-generates the account_id.
        String sql = "INSERT INTO Account (username, password) VALUES (?, ?);";
        
        // Try-with-resources statement automatically handles closing resources.
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            // Set the username and password in the prepared statement.
            stmt.setString(1, account.getUsername());
            stmt.setString(2, account.getPassword());
            
            // Execute the update and check if a row was affected.
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating account failed, no rows affected.");
            }
            
            // Retrieve the generated keys to get the account_id.
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Set the account_id back on the account object.
                    account.setAccount_id(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
        }
        // Return the account with the ID set.
        return account;
    }

    /**
     * Retrieves an Account by username.
     * 
     * @param username The username of the account to retrieve.
     * @return An Account object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public Account getAccountByUsername(String username) throws SQLException {
        Account account = null;
        String sql = "SELECT * FROM Account WHERE username = ?;";
        
        // Try-with-resources statement for resource management.
        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Set the username in the prepared statement.
            stmt.setString(1, username);
            
            // Execute the query.
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Construct an account object from the result set.
                    account = new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        // Return the found account or null.
        return account;
    }

    /**
     * Retrieves an Account by account_id.
     * 
     * @param accountId The ID of the account to retrieve.
     * @return An Account object if found, null otherwise.
     * @throws SQLException if a database access error occurs.
     */
    public Account getAccountById(int accountId) throws SQLException {
        Account account = null;
        String sql = "SELECT * FROM Account WHERE account_id = ?;";

        try (Connection conn = ConnectionUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, accountId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    account = new Account(rs.getInt("account_id"), rs.getString("username"), rs.getString("password"));
                }
            }
        }
        return account;
    }
}
