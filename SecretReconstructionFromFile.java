import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SecretReconstructionFromFile {

    public static void main(String[] args) {
        try {
            // Path to the JSON files for both test cases (you can provide your own paths)
            File testCase1 = new File("C:\\Users\\srika\\OneDrive\\Desktop\\cat\\testcase1.json");  // JSON file for first test case
            File testCase2 = new File("C:\\Users\\srika\\OneDrive\\Desktop\\cat\\testcase2.json");  // JSON file for second test case

            // Process both test cases
            System.out.println("Processing Test Case 1:");
            processTestCase(testCase1, false);  // Process the first test case, without error tracking

            System.out.println("\nProcessing Test Case 2:");
            processTestCase(testCase2, true);   // Process the second test case, with error tracking

        } catch (Exception e) {
            System.err.println("Error processing test cases: " + e.getMessage());
        }
    }

    // Function to process a single test case
    private static void processTestCase(File jsonFile, boolean trackInvalidPoints) {
        try {
            // Read the entire JSON file as a string
            String jsonString = new String(Files.readAllBytes(jsonFile.toPath()));

            // Manually parse n and k from the "keys" section
            int n = Integer.parseInt(getValueFromJson(jsonString, "\"n\""));
            int k = Integer.parseInt(getValueFromJson(jsonString, "\"k\""));

            List<Share> shares = new ArrayList<>();
            List<String> invalidPoints = new ArrayList<>(); // Track invalid points for the second test case

            // Manually parse each share and its base and value
            for (int i = 1; i <= n; i++) {
                String baseKey = "\"" + i + "\"";
                if (jsonString.contains(baseKey)) {
                    int base = Integer.parseInt(getValueFromJson(jsonString, "\"base\"", baseKey));
                    String valueStr = getValueFromJson(jsonString, "\"value\"", baseKey);

                    // Attempt to parse the value using the given base
                    try {
                        BigInteger value = new BigInteger(valueStr, base);
                        BigInteger x = BigInteger.valueOf(i);  // Use the index as x
                        shares.add(new Share(x, value));
                    } catch (NumberFormatException e) {
                        if (trackInvalidPoints) {
                            invalidPoints.add("Wrong point " + i + " with value '" + valueStr + "' under base " + base);
                        }
                    }
                }
            }

            // Use the first k valid shares to reconstruct the secret
            List<Share> usedShares = shares.subList(0, k);  // Using the first k valid shares
            BigInteger reconstructedSecret = reconstructSecret(usedShares, k);

            // Output the reconstructed secret
            System.out.println("Reconstructed Secret: " + reconstructedSecret);

            // Print invalid points if any (only for second test case)
            if (trackInvalidPoints && !invalidPoints.isEmpty()) {
                System.out.println("Invalid Points:");
                for (String invalidPoint : invalidPoints) {
                    System.out.println(invalidPoint);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
        }
    }

    // Helper method to extract a value from JSON
    private static String getValueFromJson(String json, String key) {
        int startIndex = json.indexOf(key) + key.length() + 2;
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {  // If it's the last value in the JSON
            endIndex = json.indexOf("}", startIndex);
        }
        return json.substring(startIndex, endIndex).replaceAll("[^0-9]", "").trim();
    }

    // Helper method to extract a value with a parent key
    private static String getValueFromJson(String json, String key, String parentKey) {
        int parentIndex = json.indexOf(parentKey);
        return getValueFromJson(json.substring(parentIndex), key);
    }

    // Method to reconstruct the secret using k shares
    public static BigInteger reconstructSecret(List<Share> shares, int k) {
        BigInteger PRIME_MODULUS = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16); // Large prime modulus

        BigInteger secret = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger xi = shares.get(i).getX();
            BigInteger yi = shares.get(i).getY();

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigInteger xj = shares.get(j).getX();
                    numerator = numerator.multiply(xj.negate()).mod(PRIME_MODULUS);
                    denominator = denominator.multiply(xi.subtract(xj)).mod(PRIME_MODULUS);
                }
            }

            BigInteger lagrange = numerator.multiply(denominator.modInverse(PRIME_MODULUS)).mod(PRIME_MODULUS);
            secret = secret.add(yi.multiply(lagrange)).mod(PRIME_MODULUS);
        }

        return secret.mod(PRIME_MODULUS);
    }

    // Share class to hold x and y values
    public static class Share {
        private final BigInteger x;
        private final BigInteger y;

        public Share(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        public BigInteger getX() {
            return x;
        }

        public BigInteger getY() {
            return y;
        }

        @Override
        public String toString() {
            return "Share{" + "x=" + x + ", y=" + y + '}';
        }
    }
}
