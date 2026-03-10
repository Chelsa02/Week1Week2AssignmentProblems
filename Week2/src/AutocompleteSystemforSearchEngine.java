import java.util.*;

public class AutocompleteSystemforSearchEngine {

    class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
        String word = "";
    }

    private TrieNode root = new TrieNode();

    // Global frequency map
    private Map<String, Integer> frequencyMap = new HashMap<>();

    // Insert a query into the Trie
    public void insert(String query) {
        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }

        node.isWord = true;
        node.word = query;
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0));
    }

    // Update frequency (new search)
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insert(query);
    }

    // Suggest top K for prefix
    public List<String> search(String prefix) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return new ArrayList<>();
            node = node.children.get(c);
        }

        PriorityQueue<String> minHeap = new PriorityQueue<>(
                (a, b) -> {
                    int freqA = frequencyMap.getOrDefault(a, 0);
                    int freqB = frequencyMap.getOrDefault(b, 0);
                    if (freqA != freqB) return freqA - freqB; // min-heap
                    return b.compareTo(a); // tie-breaker
                }
        );

        dfs(node, minHeap, 10);

        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) result.add(0, minHeap.poll());

        return result;
    }

    // DFS traversal to collect words
    private void dfs(TrieNode node, PriorityQueue<String> heap, int k) {
        if (node.isWord) {
            heap.offer(node.word);
            if (heap.size() > k) heap.poll();
        }

        for (TrieNode child : node.children.values()) {
            dfs(child, heap, k);
        }
    }

    public static void main(String[] args) {

        AutocompleteSystemforSearchEngine autocomplete = new AutocompleteSystemforSearchEngine();

        // Simulate adding queries
        autocomplete.updateFrequency("java tutorial");
        autocomplete.updateFrequency("javascript");
        autocomplete.updateFrequency("java download");
        autocomplete.updateFrequency("java tutorial");
        autocomplete.updateFrequency("java 21 features");

        // Search for prefix
        List<String> suggestions = autocomplete.search("jav");

        System.out.println("Suggestions for 'jav':");
        for (String s : suggestions) {
            System.out.println(s + " (" + autocomplete.frequencyMap.get(s) + " searches)");
        }

        // Update frequency
        autocomplete.updateFrequency("java 21 features");
        autocomplete.updateFrequency("java 21 features");
        System.out.println("\nAfter updating frequency for 'java 21 features':");
        suggestions = autocomplete.search("jav");
        for (String s : suggestions) {
            System.out.println(s + " (" + autocomplete.frequencyMap.get(s) + " searches)");
        }
    }
}