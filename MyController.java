package com.medicos.web.Medicos_Medical.Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.medicos.web.Medicos_Medical.dbConnection.DbConnection;
import com.medicos.web.Medicos_Medical.services.LoginService;

@RestController
public class MyController {

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("/login")
    public ResponseEntity<HashMap<String, Object>> login(@RequestBody HashMap<String, String> map) {
        HashMap<String, Object> response = new HashMap<>();
        String role = LoginService.isValidUser(map.get("email"), map.get("password"));
        
        if (role != null && !role.isBlank()) {
            response.put("status", "success");
            response.put("message", "Login successful");
            response.put("role", role);
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid Credentials");
            
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
    }
	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@PostMapping("/add-staff")
	public ResponseEntity<HashMap<String, Object>> addStaff(@RequestBody List<HashMap<String, String>> staffDetails) {
	    HashMap<String, Object> response = new HashMap<>();

	    try (Connection con = DbConnection.getConnection()) {
	        String query = "INSERT INTO STAFF (NAME, EMAIL, PASSWORD, LOCATION, CONTACT) VALUES (?, ?, ?, ?, ?)";
	        PreparedStatement ps = con.prepareStatement(query);

	        for (HashMap<String, String> staff : staffDetails) {
	            String name = staff.get("name");
	            String email = staff.get("email");
	            String password = staff.get("password");
	            String location = staff.get("location");
	            String contact = staff.get("contact");

	            if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank()) {
	                response.put("status", "error");
	                response.put("message", "All fields are required for each staff member");
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	            }

	            ps.setString(1, name);
	            ps.setString(2, email);
	            ps.setString(3, password);
	            ps.setString(4, location);
	            ps.setString(5, contact);
	            ps.addBatch();
	        }

	        ps.executeBatch();
	        response.put("status", "success");
	        response.put("message", "Staff members added successfully");
	        return ResponseEntity.status(HttpStatus.OK).body(response);
	    } catch (SQLException | ClassNotFoundException e) {
	        e.printStackTrace();
	        response.put("status", "error");
	        response.put("message", "Database error occurred");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}
	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@GetMapping("/view-staff")
	public ResponseEntity<List<HashMap<String, Object>>> viewStaff() {
	    List<HashMap<String, Object>> staffList = new ArrayList<>();
	    try (Connection con = DbConnection.getConnection()) {
	        String query = "SELECT NAME, EMAIL, LOCATION, CONTACT, PASSWORD FROM STAFF"; // Include PASSWORD
	        PreparedStatement ps = con.prepareStatement(query);
	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	            HashMap<String, Object> staff = new HashMap<>();
	            staff.put("name", rs.getString("NAME"));
	            staff.put("email", rs.getString("EMAIL"));
	            staff.put("location", rs.getString("LOCATION"));
	            staff.put("contact", rs.getString("CONTACT"));
	            staff.put("password", rs.getString("PASSWORD")); // Ensure column exists
	            staffList.add(staff);
	        }
	        return ResponseEntity.status(HttpStatus.OK).body(staffList);
	    } catch (SQLException | ClassNotFoundException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}


	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@PostMapping("/add-medicine")
	public ResponseEntity<HashMap<String, Object>> addMedicine(@RequestBody HashMap<String, String> medicineDetails) {
	    HashMap<String, Object> response = new HashMap<>();
	    try (Connection con = DbConnection.getConnection()) {

	        // Validate fields
	        if (!isValidInput(medicineDetails)) {
	            response.put("status", "error");
	            response.put("message", "All fields are required and must be valid!");
	            return ResponseEntity.badRequest().body(response);
	        }

	        String query = "INSERT INTO MEDICINE (NAME, COMPOSITION, EXPIRY, QUANTITY, PRICE) VALUES (?, ?, ?, ?, ?)";
	        PreparedStatement ps = con.prepareStatement(query);

	        ps.setString(1, medicineDetails.get("name"));
	        ps.setString(2, medicineDetails.get("composition"));
	        ps.setDate(3, java.sql.Date.valueOf(medicineDetails.get("expiry"))); // Convert expiry to SQL date
	        ps.setInt(4, Integer.parseInt(medicineDetails.get("quantity")));
	        ps.setDouble(5, Double.parseDouble(medicineDetails.get("price")));

	        // Execute query
	        int rowsInserted = ps.executeUpdate();
	        if (rowsInserted > 0) {
	            response.put("status", "success");
	            response.put("message", "Medicine added successfully!");
	            return ResponseEntity.status(HttpStatus.OK).body(response);
	        } else {
	            response.put("status", "error");
	            response.put("message", "Failed to add medicine. Please try again.");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }

	    } catch (IllegalArgumentException e) {
	        response.put("status", "error");
	        response.put("message", "Invalid date format for expiry. Expected format: yyyy-MM-dd.");
	        return ResponseEntity.badRequest().body(response);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        response.put("status", "error");
	        response.put("message", "Database error: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.put("status", "error");
	        response.put("message", "Unexpected error: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}

	private boolean isValidInput(HashMap<String, String> medicineDetails) {
	    try {
	        // Check if fields are present and valid
	        if (medicineDetails.get("name") == null || medicineDetails.get("name").trim().isEmpty()) return false;
	        if (medicineDetails.get("composition") == null || medicineDetails.get("composition").trim().isEmpty()) return false;
	        if (medicineDetails.get("expiry") == null || medicineDetails.get("expiry").trim().isEmpty()) return false;
	        if (medicineDetails.get("quantity") == null || medicineDetails.get("quantity").trim().isEmpty()) return false;
	        if (medicineDetails.get("price") == null || medicineDetails.get("price").trim().isEmpty()) return false;

	        // Validate expiry date format
	        java.sql.Date.valueOf(medicineDetails.get("expiry"));

	        // Validate quantity and price
	        Integer.parseInt(medicineDetails.get("quantity"));
	        Double.parseDouble(medicineDetails.get("price"));
	        return true;
	    } catch (Exception e) {
	        System.out.println("Validation error: " + e.getMessage());
	        return false;
	    }
	}


	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@GetMapping("/edit-medicines")
	public ResponseEntity<List<Map<String, Object>>> editMedicines() throws ClassNotFoundException {
	    List<Map<String, Object>> medicines = new ArrayList<>();
	    try (Connection con = DbConnection.getConnection();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery("SELECT * FROM MEDICINE")) {

	        while (rs.next()) {
	            Map<String, Object> medicine = new HashMap<>();
	            medicine.put("name", rs.getString("NAME"));
	            medicine.put("composition", rs.getString("COMPOSITION"));
	            medicine.put("expiry", rs.getDate("EXPIRY") != null ? rs.getDate("EXPIRY").toString() : "N/A");
	            medicine.put("quantity", rs.getInt("QUANTITY"));
	            medicine.put("price", rs.getDouble("PRICE"));
	            medicines.add(medicine);
	        }
	        return ResponseEntity.ok(medicines);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}


	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@DeleteMapping("/delete-medicine/{name}")
	public ResponseEntity<Map<String, String>> deleteMedicine(@PathVariable String name) throws ClassNotFoundException {
	    System.out.println("Received DELETE request for medicine: " + name); // Debug log
	    try (Connection con = DbConnection.getConnection()) {
	        String query = "DELETE FROM MEDICINE WHERE NAME = ?";
	        PreparedStatement ps = con.prepareStatement(query);
	        ps.setString(1, name);

	        int rowsDeleted = ps.executeUpdate();
	        System.out.println("Rows deleted: " + rowsDeleted); // Debug log

	        if (rowsDeleted > 0) {
	            return ResponseEntity.ok(Map.of("message", "Medicine deleted successfully."));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Medicine not found."));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Database error: " + e.getMessage()));
	    }
	}


	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@PutMapping("/edit-medicine")
	public ResponseEntity<Map<String, String>> editMedicine(@RequestBody Map<String, Object> medicineDetails) throws ClassNotFoundException {
	    try (Connection con = DbConnection.getConnection()) {
	        String query = "UPDATE MEDICINE SET COMPOSITION = ?, EXPIRY = ?, QUANTITY = ?, PRICE = ? WHERE NAME = ?";
	        PreparedStatement ps = con.prepareStatement(query);

	        ps.setString(1, (String) medicineDetails.get("composition"));
	        ps.setDate(2, java.sql.Date.valueOf((String) medicineDetails.get("expiry")));
	        ps.setInt(3, Integer.parseInt(medicineDetails.get("quantity").toString()));
	        ps.setDouble(4, Double.parseDouble(medicineDetails.get("price").toString()));
	        ps.setString(5, (String) medicineDetails.get("name"));

	        int rowsUpdated = ps.executeUpdate();
	        if (rowsUpdated > 0) {
	            return ResponseEntity.ok(Map.of("message", "Medicine updated successfully."));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Medicine not found."));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	    }
	}
	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@PutMapping("/update-staff/{name}")
	public ResponseEntity<Map<String, String>> updateStaff(
	        @PathVariable String name, 
	        @RequestBody Map<String, String> staffDetails) throws ClassNotFoundException {
	    try (Connection con = DbConnection.getConnection()) {
	        String query = "UPDATE STAFF SET NAME = ?, EMAIL = ?, LOCATION = ?, CONTACT = ? WHERE NAME = ?";
	        PreparedStatement ps = con.prepareStatement(query);

	        ps.setString(1, staffDetails.get("name"));
	        ps.setString(2, staffDetails.get("email"));
	        ps.setString(3, staffDetails.get("location"));
	        ps.setString(4, staffDetails.get("contact"));
	        ps.setString(5, name);

	        int rowsUpdated = ps.executeUpdate();

	        if (rowsUpdated > 0) {
	            return ResponseEntity.ok(Map.of("message", "Staff member updated successfully."));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Staff member not found."));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	    }
	}

	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@DeleteMapping("/delete-staff/{name}")
	public ResponseEntity<Map<String, String>> deleteStaff(@PathVariable String name) throws ClassNotFoundException {
	    try (Connection con = DbConnection.getConnection()) {
	        String query = "DELETE FROM STAFF WHERE NAME = ?";
	        PreparedStatement ps = con.prepareStatement(query);
	        ps.setString(1, name);

	        int rowsDeleted = ps.executeUpdate();

	        if (rowsDeleted > 0) {
	            return ResponseEntity.ok(Map.of("message", "Staff member deleted successfully."));
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Staff member not found."));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
	    }
	}

	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@GetMapping("/view-medicine")
	public ResponseEntity<List<Map<String, Object>>> viewMedicines() throws ClassNotFoundException {
	    List<Map<String, Object>> medicines = new ArrayList<>();
	    try (Connection con = DbConnection.getConnection();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery("SELECT * FROM MEDICINE")) {

	        while (rs.next()) {
	            Map<String, Object> medicine = new HashMap<>();
	            medicine.put("name", rs.getString("NAME"));
	            medicine.put("composition", rs.getString("COMPOSITION"));
	            medicine.put("expiry", rs.getDate("EXPIRY") != null ? rs.getDate("EXPIRY").toString() : "N/A");
	            medicine.put("quantity", rs.getInt("QUANTITY"));
	            medicine.put("price", rs.getDouble("PRICE"));
	            medicines.add(medicine);
	        }
	        return ResponseEntity.ok(medicines);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	    }
	}
	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@PostMapping("/buy-medicine")
	public ResponseEntity<String> buyMedicine(@RequestBody Map<String, Object> purchaseDetails) throws ClassNotFoundException {
	    String name = (String) purchaseDetails.get("name");
	    String composition = (String) purchaseDetails.get("composition");
	    int purchaseQuantity = (int) purchaseDetails.get("quantity");

	    try (Connection con = DbConnection.getConnection();
	         PreparedStatement selectStmt = con.prepareStatement("SELECT QUANTITY FROM MEDICINE WHERE NAME = ?");
	         PreparedStatement updateStmt = con.prepareStatement("UPDATE MEDICINE SET QUANTITY = QUANTITY - ? WHERE NAME = ?");
	         PreparedStatement insertStmt = con.prepareStatement("INSERT INTO SALES (NAME, COMPOSITION, QUANTITY) VALUES (?, ?, ?)")) {

	        // Check available quantity
	        selectStmt.setString(1, name);
	        ResultSet rs = selectStmt.executeQuery();
	        if (rs.next()) {
	            int availableQuantity = rs.getInt("QUANTITY");
	            if (availableQuantity < purchaseQuantity) {
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock.");
	            }
	        } else {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Medicine not found.");
	        }

	        // Update quantity in MEDICINE table
	        updateStmt.setInt(1, purchaseQuantity);
	        updateStmt.setString(2, name);
	        updateStmt.executeUpdate();

	        // Insert purchase details into SALES table
	        insertStmt.setString(1, name);
	        insertStmt.setString(2, composition);
	        insertStmt.setInt(3, purchaseQuantity);
	        insertStmt.executeUpdate();

	        return ResponseEntity.ok("Purchase successful.");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the purchase.");
	    }
	}
	
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	@GetMapping("/view-sales")
	public ResponseEntity<List<Map<String, Object>>> viewSales() throws ClassNotFoundException {
	    List<Map<String, Object>> sales = new ArrayList<>();
	    try (Connection con = DbConnection.getConnection();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery("SELECT * FROM SALES")) {

	        while (rs.next()) {
	            Map<String, Object> sale = new HashMap<>();
	            sale.put("name", rs.getString("NAME"));
	            sale.put("composition", rs.getString("COMPOSITION"));
	            sale.put("quantity", rs.getInt("QUANTITY"));
	            sale.put("sold", rs.getDate("SOLD")); // Ensure DATE is a timestamp in SALES table
	            sales.add(sale);
	        }
	        return ResponseEntity.ok(sales);
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	    }
	}

}

