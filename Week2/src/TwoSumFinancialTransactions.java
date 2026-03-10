import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TwoSumFinancialTransactions {

    static class Transaction {
        int id;
        double amount;
        String merchant;
        String account;
        LocalDateTime time;

        Transaction(int id, double amount, String merchant, String account, String timeStr) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            this.time = LocalDateTime.parse(timeStr, formatter);
        }
    }

    List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // Classic Two-Sum
    public List<int[]> findTwoSum(double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }

        return result;
    }

    // Two-Sum within 1-hour window
    public List<int[]> findTwoSumTimeWindow(double target, int windowMinutes) {
        List<int[]> result = new ArrayList<>();

        transactions.sort(Comparator.comparing(t -> t.time));

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);

                long minutes = java.time.Duration.between(t1.time, t2.time).toMinutes();

                if (minutes > windowMinutes) break;

                if (t1.amount + t2.amount == target) {
                    result.add(new int[]{t1.id, t2.id});
                }
            }
        }

        return result;
    }

    // K-Sum recursive
    public List<List<Integer>> findKSum(int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        findKSumHelper(transactions, k, 0, target, new ArrayList<>(), result);
        return result;
    }

    private void findKSumHelper(List<Transaction> list, int k, int index, double target,
                                List<Integer> path, List<List<Integer>> result) {
        if (k == 0 && Math.abs(target) < 0.0001) {
            result.add(new ArrayList<>(path));
            return;
        }
        if (k == 0 || index >= list.size()) return;

        // include current transaction
        path.add(list.get(index).id);
        findKSumHelper(list, k - 1, index + 1, target - list.get(index).amount, path, result);
        path.remove(path.size() - 1);

        // exclude current transaction
        findKSumHelper(list, k, index + 1, target, path, result);
    }

    // Duplicate detection
    public List<Map<String, Object>> detectDuplicates() {
        Map<String, List<Transaction>> map = new HashMap<>();
        List<Map<String, Object>> duplicates = new ArrayList<>();

        for (Transaction t : transactions) {
            String key = t.amount + "_" + t.merchant;
            map.putIfAbsent(key, new ArrayList<>());
            map.get(key).add(t);
        }

        for (String key : map.keySet()) {
            List<Transaction> list = map.get(key);
            Set<String> accounts = new HashSet<>();
            for (Transaction t : list) accounts.add(t.account);
            if (accounts.size() > 1) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("amount", list.get(0).amount);
                entry.put("merchant", list.get(0).merchant);
                entry.put("accounts", accounts);
                duplicates.add(entry);
            }
        }

        return duplicates;
    }

    public static void main(String[] args) {

        TwoSumFinancialTransactions ts = new TwoSumFinancialTransactions();

        ts.addTransaction(new Transaction(1, 500, "Store A", "acc1", "10:00"));
        ts.addTransaction(new Transaction(2, 300, "Store B", "acc2", "10:15"));
        ts.addTransaction(new Transaction(3, 200, "Store C", "acc3", "10:30"));
        ts.addTransaction(new Transaction(4, 500, "Store A", "acc4", "11:00"));

        System.out.println("Classic Two-Sum (target=500):");
        for (int[] pair : ts.findTwoSum(500)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nTwo-Sum within 60 minutes (target=500):");
        for (int[] pair : ts.findTwoSumTimeWindow(500, 60)) {
            System.out.println(Arrays.toString(pair));
        }

        System.out.println("\nK-Sum (k=3, target=1000):");
        for (List<Integer> combination : ts.findKSum(3, 1000)) {
            System.out.println(combination);
        }

        System.out.println("\nDuplicate detection:");
        for (Map<String, Object> dup : ts.detectDuplicates()) {
            System.out.println(dup);
        }
    }
}