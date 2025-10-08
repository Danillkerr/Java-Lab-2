import java.util.*;

import org.w3c.dom.Text;

/**
 * Laboratory Work №4
 * Topic: Relations between classes in Java.
 * Goal: Create classes for Letter, Word, Punctuation, Sentence and Text,
 *       parse input text into these classes, normalize whitespace,
 *       and perform the same analysis as Lab #2: find the maximal number
 *       of sentences that contain the same word.
 *
 * Implementation details:
 *  - Word is internally an array of Letter.
 *  - Sentence is an array of Elements (Word or Punctuation).
 *  - Text is an array of Sentence.
 *  - Normalization: any sequence of spaces and tabs is replaced by a single space.
 *
 */
public class СompositionClass {

    /**
     * Represents a single character (letter/digit/apostrophe etc).
     */
    public static class Letter {
        private final char ch;

        public Letter(char ch) {
            this.ch = ch;
        }

        public char getChar() {
            return ch;
        }

        @Override
        public String toString() {
            return Character.toString(ch);
        }
    }

    /**
     * Marker interface for elements of a sentence.
     * Concrete implementations: WordElement and PunctuationElement.
     */
    public interface SentenceElement {
    }

    /**
     * Represents a word built from Letters.
     * Internally stores letters as an array.
     */
    public static class Word implements SentenceElement {
        private final Letter[] letters;

        /**
         * Build a word from a sequence of chars.
         *
         * @param chars char array for the word (must be non-empty)
         */
        public Word(char[] chars) {
            if (chars == null || chars.length == 0) {
                throw new IllegalArgumentException("Word must have at least one character.");
            }
            this.letters = new Letter[chars.length];
            for (int i = 0; i < chars.length; i++) {
                this.letters[i] = new Letter(chars[i]);
            }
        }

        /**
         * Return normalized string representation (lowercase).
         *
         * @return lowercase String of this word
         */
        public String asLowerString() {
            StringBuilder sb = new StringBuilder(letters.length);
            for (Letter l : letters) {
                sb.append(Character.toLowerCase(l.getChar()));
            }
            return sb.toString();
        }

        /**
         * Return original string representation (preserving chars order).
         *
         * @return String
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(letters.length);
            for (Letter l : letters) sb.append(l.getChar());
            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Word)) return false;
            Word other = (Word) obj;

            return this.asLowerString().equals(other.asLowerString());
        }

        @Override
        public int hashCode() {
            return Objects.hash(asLowerString());
        }
    }

    /**
     * Represents a punctuation mark (single character).
     */
    public static class Punctuation implements SentenceElement {
        private final char ch;

        public Punctuation(char ch) {
            this.ch = ch;
        }

        public char getChar() {
            return ch;
        }

        @Override
        public String toString() {
            return Character.toString(ch);
        }
    }

    /**
     * Sentence consists of an array of SentenceElement (Word or Punctuation).
     * Also provides methods to obtain Word[] (words only) and toString().
     */
    public static class Sentence {
        private final SentenceElement[] elements;

        public Sentence(SentenceElement[] elements) {
            if (elements == null) {
                throw new IllegalArgumentException("Elements array must not be null.");
            }
            this.elements = Arrays.copyOf(elements, elements.length);
        }

        /**
         * Get all elements (shallow copy).
         *
         * @return array of SentenceElement
         */
        public SentenceElement[] getElements() {
            return Arrays.copyOf(elements, elements.length);
        }

        /**
         * Extracts words (Word[]) from sentence in original order.
         *
         * @return array of Word (may be empty)
         */
        public Word[] getWords() {
            List<Word> tmp = new ArrayList<>();
            for (SentenceElement e : elements) {
                if (e instanceof Word) tmp.add((Word) e);
            }
            return tmp.toArray(new Word[0]);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            final String OPEN_PUNCT = "([{\"'`“«";

            for (int i = 0; i < elements.length; i++) {
                SentenceElement current = elements[i];
                sb.append(current.toString());

                if (i + 1 < elements.length) {
                    SentenceElement next = elements[i + 1];

                    boolean nextIsWord = next instanceof Word;
                    boolean currentIsPunct = current instanceof Punctuation;

                    if (nextIsWord) {
                        boolean putSpace = true;

                        if (currentIsPunct) {
                            char pc = ((Punctuation) current).getChar();
                            if (OPEN_PUNCT.indexOf(pc) >= 0) {
                                putSpace = false;
                            }
                        }
                        if (putSpace) {
                            sb.append(' ');
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    /**
     * Text is an array of Sentence.
     */
    public static class Text {
        private final Sentence[] sentences;

        public Text(Sentence[] sentences) {
            if (sentences == null) {
                throw new IllegalArgumentException("Sentences array must not be null.");
            }
            this.sentences = Arrays.copyOf(sentences, sentences.length);
        }

        public Sentence[] getSentences() {
            return Arrays.copyOf(sentences, sentences.length);
        }
    }

    private static final String SENTENCE_TERMINATORS = ".!?";
    private static final int MAX_TEXT_LENGTH = 20_000;

    /**
     * Replace sequences of spaces and tabs with a single space.
     *
     * @param input original text (may be null)
     * @return normalized StringBuilder (never null)
     */
    public static StringBuilder normalizeWhitespace(StringBuilder input) {
        if (input == null) return new StringBuilder();
        StringBuilder out = new StringBuilder(input.length());
        boolean lastWasSpace = false;
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch == '\t' || ch == ' ') {
                if (!lastWasSpace) {
                    out.append(' ');
                    lastWasSpace = true;
                }
            } else {
                out.append(ch);
                lastWasSpace = false;
            }
        }
        return out;
    }

    /**
     * Parse normalized text into Sentence[].
     * - Sentences terminated by . ! ?
     * - Each sentence is tokenized into Word and Punctuation elements
     *
     * @param text normalized text (StringBuilder)
     * @return Sentence[] (may be empty if no sentences)
     */
    public static Sentence[] parseTextToSentences(StringBuilder text) {
        if (text == null) return new Sentence[0];
        List<Sentence> sentencesList = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            current.append(ch);
            if (SENTENCE_TERMINATORS.indexOf(ch) >= 0) {
                Sentence s = buildSentenceFromRaw(current.toString().trim());
                if (s != null) sentencesList.add(s);
                current.setLength(0);
            }
        }

        String leftover = current.toString().trim();
        if (!leftover.isEmpty()) {
            Sentence s = buildSentenceFromRaw(leftover);
            if (s != null) sentencesList.add(s);
        }
        return sentencesList.toArray(new Sentence[0]);
    }

    /**
     * Build a Sentence from raw text (single sentence string).
     * Tokenizes into Word and Punctuation elements.
     *
     * @param rawSentence trimmed raw sentence (non-null or "")
     * @return Sentence or null if empty
     */
    public static Sentence buildSentenceFromRaw(String rawSentence) {
        if (rawSentence == null) return null;
        if (rawSentence.isEmpty()) return null;

        List<SentenceElement> elements = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();

        for (int i = 0; i < rawSentence.length(); i++) {
            char ch = rawSentence.charAt(i);
            if (Character.isLetterOrDigit(ch) || ch == '\'') {
                currentWord.append(ch);
            } else {
                if (currentWord.length() > 0) {
                    char[] arr = toCharArray(currentWord);
                    elements.add(new Word(arr));
                    currentWord.setLength(0);
                }
                if (!Character.isWhitespace(ch)) {
                    elements.add(new Punctuation(ch));
                }
            }
        }

        if (currentWord.length() > 0) {
            elements.add(new Word(toCharArray(currentWord)));
        }

        if (elements.isEmpty()) return null;

        SentenceElement[] arr = elements.toArray(new SentenceElement[0]);
        return new Sentence(arr);
    }

    private static char[] toCharArray(StringBuilder sb) {
        char[] arr = new char[sb.length()];
        for (int i = 0; i < sb.length(); i++) arr[i] = sb.charAt(i);
        return arr;
    }

    /**
     * Analyze Text: compute map word -> set of sentence indices where the word appears.
     * Word equality is case-insensitive (Word.asLowerString()).
     *
     * @param textObj Text object
     * @return map of normalized word -> set of sentence indices
     */
    public static Map<String, Set<Integer>> analyzeText(Text textObj) {
        Map<String, Set<Integer>> wordToSentences = new HashMap<>();
        if (textObj == null) return wordToSentences;
        Sentence[] sentences = textObj.getSentences();
        for (int si = 0; si < sentences.length; si++) {
            Sentence s = sentences[si];
            Word[] words = s.getWords();
            Set<String> unique = new HashSet<>();
            for (Word w : words) {
                String norm = w.asLowerString();
                unique.add(norm);
            }
            for (String norm : unique) {
                Set<Integer> set = wordToSentences.computeIfAbsent(norm, k -> new HashSet<>());
                set.add(si);
            }
        }
        return wordToSentences;
    }

    /**
     * Find maximum number of sentences that contain the same word and return words that achieve it.
     *
     * @param wordToSentences map word->set of sentence indices
     * @return pair: (maxCount, Set of words achieving maxCount)
     */
    public static Map.Entry<Integer, Set<String>> findMaxWords(Map<String, Set<Integer>> wordToSentences) {
        int max = 0;
        Set<String> words = new HashSet<>();
        for (Map.Entry<String, Set<Integer>> e : wordToSentences.entrySet()) {
            int size = e.getValue().size();
            if (size > max) {
                max = size;
                words.clear();
                words.add(e.getKey());
            } else if (size == max) {
                words.add(e.getKey());
            }
        }
        return new AbstractMap.SimpleEntry<>(max, words);
    }


    /**
     * Program entry. All execution variables are declared and assigned here.
     *
     * Interactive loop: after each run asks whether to exit.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        System.out.println("=== Lab #4: Relations between classes (Text -> Sentence -> Word -> Letter) ===");
        System.out.println("Enter a text (press Enter for sample). Sequences of spaces/tabs will be collapsed to a single space.");

        try (Scanner scanner = new Scanner(System.in)) {
            boolean exit = false;
            while (!exit) {
                StringBuilder rawInput;
                System.out.print("\nEnter text (or press Enter for default): ");
                String line = scanner.nextLine();
                if (line == null || line.trim().isEmpty()) {
                    String sample = "The quick, brown fox jumps over a lazy dog. DJs flock by when MTV ax quiz prog. " +
                    "Junk MTV quiz graced by fox whelps. Bawds jog, flick quartz, vex nymphs. Waltz, bad nymph, for " +
                    "quick jigs vex! Fox nymphs grab quick-jived waltz. Brick quiz whangs jumpy veldt fox. Bright vixens " +
                    "jump; dozy fowl quack. Quick wafting zephyrs vex bold Jim. Quick zephyrs blow, vexing daft Jim. " +
                    "Sex-charged fop blew my junk TV quiz. How quickly daft jumping zebras vex. Two driven jocks help fax " +
                    "my big quiz. Quick, Baz, get my woven flax jodhpurs! Five " +
                    "quacking zephyrs jolt my wax bed. Flummoxed by job, kvetching W. zaps Iraq. Cozy sphinx waves quart " +
                    "jug of bad milk. A very bad quack might jinx zippy fowls. Few quips galvanized the mock jury box. Quick " +
                    "brown dogs jump over the lazy fox. The jay, pig, fox, zebra, and my wolves quack! Blowzy red vixens fight " +
                    "for a quick jump. Joaquin Phoenix was gazed by MTV for luck. A wizards job is to vex chumps quickly in fog. " +
                    "Watch Jeopardy! Alex fun TV quiz game. Woven silk pyjamas exchanged for blue quartz.";
                    rawInput = new StringBuilder(sample);
                    System.out.println("\nUsing sample text:");
                    System.out.println(rawInput.toString());
                } else {
                    rawInput = new StringBuilder(line);
                }

                StringBuilder normalized = normalizeWhitespace(rawInput);
                if (normalized.length() > MAX_TEXT_LENGTH) {
                    System.err.println("Input too long. Skipping.");
                } else {
                    Sentence[] sentences = parseTextToSentences(normalized);
                    Text textObj = new Text(sentences);

                    Map<String, Set<Integer>> wordToSentences = analyzeText(textObj);
                    Map.Entry<Integer, Set<String>> maxEntry = findMaxWords(wordToSentences);
                    int maxCount = maxEntry.getKey();
                    Set<String> maxWords = maxEntry.getValue();

                    System.out.println("\nParsed sentences count: " + sentences.length);
                    System.out.println("Maximum number of sentences that contain the same word: " + maxCount);

                    if (maxCount > 0 && !maxWords.isEmpty()) {
                        List<String> sorted = new ArrayList<>(maxWords);
                        Collections.sort(sorted);
                        System.out.println("Words with this property:");
                        for (String w : sorted) {
                            System.out.println(" - " + w + " (sentences: " + wordToSentences.get(w) + ")");
                        }
                    } else {
                        System.out.println("No words found.");
                    }

                    System.out.println("\nParsed structure (sentences):");
                    for (int i = 0; i < sentences.length; i++) {
                        System.out.printf("[%d] %s%n", i, sentences[i].toString());
                    }
                }

                String choice;
                do {
                    System.out.print("\nDo you want to exit? (y/n): ");
                    choice = scanner.nextLine().trim().toLowerCase();
                } while (!choice.equals("y") && !choice.equals("n"));
                if (choice.equals("y")) {
                    exit = true;
                } else {
                    System.out.println("Repeating...");
                }
            }
        }
    }
}
