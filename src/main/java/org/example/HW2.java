//21812062 김주환
package org.example;
import java.io.*;
import java.util.*;

class User implements Comparable<User> {
    String doc;
    double similarity;

    public User(String key, double cosSim) {
        this.doc = key;
        this.similarity = cosSim;
    }

    @Override
    public int compareTo(User o) {
        if (this.similarity < o.similarity) {
            return 1;
        } else if (this.similarity > o.similarity) {
            return -1;
        } else {
            return 0;
        }
    }
}

class Doc {
    HashMap<String, HashMap<Integer, Integer>> documents;
    HashMap<Integer, Integer> freq = new HashMap<>();


    Doc(HashMap doc, String DocName, int N, HashMap freq) {
        this.documents = doc;
        this.freq = freq;


        HashMap<String, HashMap<Integer, Double>> temp = new HashMap<>();
        temp = TFIDF();

        ArrayList<User> DocSim = new ArrayList<>();
        ArrayList<Integer> word = new ArrayList<>(temp.get(DocName).keySet());

        for (Map.Entry<String, HashMap<Integer, Double>> entry : temp.entrySet()) {
            if (!entry.getKey().equals(DocName)) {
                User user = new User(entry.getKey(), cosSim(DocName, entry.getKey(), temp));
                DocSim.add(user);
            }
        }

        Collections.sort(DocSim);
        Collections.sort(word);

        System.out.println("결과 1. \"" + DocName + "\"의 TF-IDF 벡터");
        System.out.printf("[ ");

        for (int i = 0; i < word.size(); i++) {
            Integer wordHash = word.get(i);
            System.out.printf("(%d, %.3f) ", wordHash, temp.get(DocName).get(wordHash));
        }

        System.out.printf("]\n\n");

        System.out.println("결과 2. \"" + DocName + "\"과(와) 유사한 " + N + "개의 문서");
        for (int i = 0; i < N; i++) {
            System.out.printf("%d. %s (유사도=%.5f)\n", i + 1, DocSim.get(i).doc, DocSim.get(i).similarity);
        }

    }

    private double TF(int word, String name) {
        int total = 0;

        for (Map.Entry<Integer, Integer> v : documents.get(name).entrySet()) {
            total += v.getValue();
        }

        if (total == 0) {
            return 0;
        }

        return (double) (documents.get(name).get(word)) / total;
    }

    private double IDF(int word) {

        if (!freq.containsKey(word)){
            return 0;
        }

        return Math.log((double) documents.size() / freq.get(word));
    }

    private HashMap<String, HashMap<Integer, Double>> TFIDF() {
        HashMap<String, HashMap<Integer, Double>> tempHash = new HashMap<>();

        for (Map.Entry<String, HashMap<Integer, Integer>> ent : documents.entrySet()) {
            HashMap<Integer, Double> temp = new HashMap<>();
            for (Map.Entry<Integer, Integer> word : documents.get(ent.getKey()).entrySet()) {
                double v = TF(word.getKey(), ent.getKey()) * IDF(word.getKey());
                temp.put(word.getKey(), v);
                tempHash.put(ent.getKey(), temp);
            }
        }
        return tempHash;
    }

    public double cosSim(String n1, String n2, HashMap<String, HashMap<Integer, Double>> t1) {
        double share = 0;
        double r1 = 0;
        double r2 = 0;
        double remainder = 0;

        for (Map.Entry<Integer, Double> entry : t1.get(n1).entrySet()) {
            if (t1.get(n2).containsKey(entry.getKey())) {
                share += entry.getValue() * t1.get(n2).get(entry.getKey());
            }
            r1 += Math.pow(entry.getValue(), 2);
        }

        for (Map.Entry<Integer, Double> entry : t1.get(n2).entrySet()) {
            r2 += Math.pow(entry.getValue(), 2);
        }

        remainder = Math.sqrt(r1) * Math.sqrt(r2);

        if (remainder == 0) {
            return 0;
        } else {
            return share / remainder;
        }
    }
}

public class HW2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.printf("파일 이름, k, 문서 제목: ");
        String file = sc.next();
        int k = sc.nextInt();
        String DocName = sc.nextLine().substring(1);
        long beforeTime = System.currentTimeMillis();
        try {
            sc = new Scanner(new BufferedReader(new FileReader(file)));

            HashMap<String, HashMap<Integer, Integer>> doc = new HashMap<>();
            HashMap<Integer, Integer> wordFreq = new HashMap<>();

            while (sc.hasNextLine()) {
                HashMap<Integer, Integer> main = new HashMap<>();

                String name = sc.nextLine();

                String mainText[] = sc.nextLine().split("[,.?!:\"\\s]+");

                for (String str : mainText) {
                    int h = str.toLowerCase().hashCode();

                    if (!main.containsKey(h)) {
                        main.put(h, 1);
                    } else {
                        main.put(h, main.get(h) + 1);
                    }
                }
                doc.put(name, main);
            }

            sc = new Scanner(new File("stopwords.txt"));

            while (sc.hasNextLine()) {
                String stopWords = sc.nextLine();

                for (Map.Entry<String, HashMap<Integer, Integer>> ent : doc.entrySet()) {

                    if (ent.getValue().containsKey(stopWords.hashCode())) {
                        doc.get(ent.getKey()).remove(stopWords.hashCode());
                    }
                }
            }

            for (Map.Entry<String, HashMap<Integer, Integer>> ent : doc.entrySet()) {
                for (int num : ent.getValue().keySet()) {
                    if (!wordFreq.containsKey(num)) {
                        wordFreq.put(num, 1);
                    } else {
                        wordFreq.put(num, wordFreq.get(num) + 1);
                    }
                }
            }
            Doc document = new Doc(doc, DocName, k, wordFreq);
            long afterTime = System.currentTimeMillis();
            System.out.println("시간차이(m) : "+(afterTime-beforeTime));
        } catch (Exception e) {
            System.out.println("오류가 발생 했습니다.");
            e.printStackTrace();
        }
    }
}