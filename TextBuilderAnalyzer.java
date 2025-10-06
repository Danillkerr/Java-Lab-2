import java.util.*;

/**
 * Laboratory Work №2
 * Topic: Strings in Java
 * Goal: Learn about strings and basic string-processing methods using StringBuilder.
 *
 * This program:
 *  - uses StringBuilder for processing string content,
 *  - finds the maximal number of sentences in which the same word appears,
 *  - prints that maximum and the word(s) that achieve it, together with sentence indices.
 *
 * The only time String is used is for input/output operations and for storing words in sets.
 *
 */
public class TextBuilderAnalyzer {

    // === CONSTANTS ===
    private static final String SENTENCE_TERMINATORS = ".!?";
    private static final int MIN_TEXT_LENGTH = 1;
    private static final int MAX_TEXT_LENGTH = 10_000;

    /**
     * Splits text into a list of sentences.
     * The returned sentences are trimmed.
     *
     * @param text the input text as StringBuilder (must not be null)
     * @return a list of sentences as StringBuilder (each non-empty)
     * @throws IllegalArgumentException if text is null or empty or contains no sentences
     */
    public static List<StringBuilder> splitIntoSentences(StringBuilder text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null.");
        }
        if (text.length() < MIN_TEXT_LENGTH) {
            throw new IllegalArgumentException("Text is empty.");
        }

        List<StringBuilder> sentences = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            current.append(ch);

            if (SENTENCE_TERMINATORS.indexOf(ch) >= 0) {
                int start = 0;
                while (start < current.length() && Character.isWhitespace(current.charAt(start))) {
                    start++;
                }
                int end = current.length() - 1;
                while (end >= start && Character.isWhitespace(current.charAt(end))) {
                    end--;
                }
                if (end >= start) {
                    StringBuilder sentence = new StringBuilder(current.substring(start, end + 1));
                    sentences.add(sentence);
                }
                current.setLength(0);
            }
        }

        int start = 0;
        while (start < current.length() && Character.isWhitespace(current.charAt(start))) start++;
        int end = current.length() - 1;
        while (end >= start && Character.isWhitespace(current.charAt(end))) end--;
        if (end >= start) {
            StringBuilder sentence = new StringBuilder(current.substring(start, end + 1));
            sentences.add(sentence);
        }

        if (sentences.isEmpty()) {
            throw new IllegalArgumentException("No sentences detected in the text.");
        }

        return sentences;
    }

    /**
     * Extracts unique words from a sentence. Words consist of letters and digits and apostrophe.
     * Non-letter/digit characters are treated as delimiters.
     * All words are converted to lowercase while building.
     *
     * @param sentence the sentence as StringBuilder (must not be null)
     * @return a Set of unique words (as String) found in the sentence, lowercased
     */
    public static Set<String> extractUniqueWords(StringBuilder sentence) {
        Set<String> words = new HashSet<>();
        if (sentence == null || sentence.length() == 0) {
            return words;
        }

        StringBuilder word = new StringBuilder();
        for (int i = 0; i < sentence.length(); i++) {
            char ch = sentence.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '\'') {
                word.append(Character.toLowerCase(ch));
            } else {
                if (word.length() > 0) {
                    words.add(word.toString());
                    word.setLength(0);
                }
            }
        }
        if (word.length() > 0) {
            words.add(word.toString());
        }

        return words;
    }

    /**
     * Analyzes the text (StringBuilder) and returns the analysis result:
     * the maximum number of sentences a single word appears in, and the set of words that achieve it.
     *
     * @param text the input text as StringBuilder
     * @return an AnalysisResult containing the maximum count and mapping word -> set of sentence indices
     * @throws IllegalArgumentException if text is null or empty
     */
    public static AnalysisResult analyzeText(StringBuilder text) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("Input text must not be null or empty.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Input text is too long.");
        }

        List<StringBuilder> sentences = splitIntoSentences(text);

        Map<String, Set<Integer>> wordToSentences = new HashMap<>();

        for (int si = 0; si < sentences.size(); si++) {
            StringBuilder sentence = sentences.get(si);
            Set<String> wordsInSentence = extractUniqueWords(sentence);
            for (String w : wordsInSentence) {
                Set<Integer> set = wordToSentences.computeIfAbsent(w, k -> new HashSet<>());
                set.add(si);
            }
        }

        int maxCount = 0;
        for (Set<Integer> set : wordToSentences.values()) {
            if (set.size() > maxCount) {
                maxCount = set.size();
            }
        }

        Set<String> maxWords = new HashSet<>();
        for (Map.Entry<String, Set<Integer>> e : wordToSentences.entrySet()) {
            if (e.getValue().size() == maxCount) {
                maxWords.add(e.getKey());
            }
        }

        return new AnalysisResult(maxCount, maxWords, wordToSentences, sentences);
    }

    /**
     * Small helper container for the analysis result.
     */
    public static class AnalysisResult {
        public final int maxSentencesCount;
        public final Set<String> wordsWithMaxCount;
        public final Map<String, Set<Integer>> wordToSentences;
        public final List<StringBuilder> sentences;

        public AnalysisResult(int maxSentencesCount,
                              Set<String> wordsWithMaxCount,
                              Map<String, Set<Integer>> wordToSentences,
                              List<StringBuilder> sentences) {
            this.maxSentencesCount = maxSentencesCount;
            this.wordsWithMaxCount = wordsWithMaxCount;
            this.wordToSentences = wordToSentences;
            this.sentences = sentences;
        }
    }

    /**
     * Program entry point.
     * 
     * The method runs an interactive loop:
     * - prompts the user for text (or uses a sample when input is empty),
     * - analyzes the text,
     * - prints results,
     * - asks the user whether to exit and repeats until the user confirms exit.
     * All execution variables are declared and assigned inside this method.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("=== Lab #2: String processing with StringBuilder ===");
        System.out.println("You can paste a multi-sentence text or press Enter to use the default sample text.");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean exit = false;

            while (!exit) {
                StringBuilder inputText;
                System.out.print("\nEnter your text (or press Enter for sample): ");
                String line = scanner.nextLine();

                if (line == null || line.trim().isEmpty()) {
                    String sample = "The quick, brown fox jumps over a lazy dog. DJs flock by when MTV ax quiz prog. " +
                    "Junk MTV quiz graced by fox whelps. Bawds jog, flick quartz, vex nymphs. Waltz, bad nymph, for " +
                    "quick jigs vex! Fox nymphs grab quick-jived waltz. Brick quiz whangs jumpy veldt fox. Bright vixens " +
                    "jump; dozy fowl quack. Quick wafting zephyrs vex bold Jim. Quick zephyrs blow, vexing daft Jim. " +
                    "Sex-charged fop blew my junk TV quiz. How quickly daft jumping zebras vex. Two driven jocks help fax " +
                    "my big quiz. Quick, Baz, get my woven flax jodhpurs! \"Now fax quiz Jack!\" my brave ghost pled. Five " +
                    "quacking zephyrs jolt my wax bed. Flummoxed by job, kvetching W. zaps Iraq. Cozy sphinx waves quart " +
                    "jug of bad milk. A very bad quack might jinx zippy fowls. Few quips galvanized the mock jury box. Quick " +
                    "brown dogs jump over the lazy fox. The jay, pig, fox, zebra, and my wolves quack! Blowzy red vixens fight " +
                    "for a quick jump. Joaquin Phoenix was gazed by MTV for luck. A wizard’s job is to vex chumps quickly in fog. " +
                    "Watch \"Jeopardy!\", Alex fun TV quiz game. Woven silk pyjamas exchanged for blue quartz.";
                    inputText = new StringBuilder(sample);
                    System.out.println("\nUsing sample text:");
                    System.out.println(inputText.toString());
                } else {
                    inputText = new StringBuilder(line);
                }

                try {
                    AnalysisResult result = analyzeText(inputText);

                    System.out.println("\nTotal sentences detected: " + result.sentences.size());
                    System.out.println("Maximum number of sentences containing the same word: " + result.maxSentencesCount);

                    if (result.maxSentencesCount > 0) {
                        System.out.println("Word(s) that appear in " + result.maxSentencesCount + " sentence(s):");
                        List<String> sortedWords = new ArrayList<>(result.wordsWithMaxCount);
                        Collections.sort(sortedWords);
                        for (String w : sortedWords) {
                            System.out.println(" - " + w + " (appears in sentences: " + result.wordToSentences.get(w) + ")");
                        }
                    } else {
                        System.out.println("No words found in sentences.");
                    }
                } catch (IllegalArgumentException e) {
                    System.err.println("Input error: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                }

                String choice;
                do {
                    System.out.print("\nDo you want to exit? (y/n): ");
                    choice = scanner.nextLine().trim().toLowerCase();
                } while (!choice.equals("y") && !choice.equals("n"));

                if (choice.equals("y")) {
                    exit = true;
                    System.out.println("Program finished.");
                } else {
                    System.out.println("Repeating the program...");
                }
            }
        }
    }
}
