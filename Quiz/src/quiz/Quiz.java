package quiz;

import java.util.HashMap;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Giacomo
 */
public class Quiz {

    private String[][] questionAnswerMatr;

    //Constructor for the Quiz knowledge base, the integer parameter is used for deciding the text file to be read.
    public Quiz(int who) throws FileNotFoundException {
        String pathname = "";
        BufferedReader br = null;
        String st;

        int s = 0;
        switch (who) {
            case 0:
                pathname = "C:\\Users\\Giacomo\\Desktop\\Magistra\\AGENTI INTELLIGENTI\\backup progetto Jade\\mas-quiz-master\\questions\\questions-answers.txt";
                s = 1;
                break;
            case 1:
                pathname = "C:\\Users\\Giacomo\\Desktop\\Magistra\\AGENTI INTELLIGENTI\\backup progetto Jade\\mas-quiz-master\\questions\\contestant-one-kb.txt";
                s = 2;
                break;
            case 2:
                pathname = "C:\\Users\\Giacomo\\Desktop\\Magistra\\AGENTI INTELLIGENTI\\backup progetto Jade\\mas-quiz-master\\questions\\contestant-two-kb.txt";
                s = 3;
                break;
        }

        File file = new File(pathname);
        br = new BufferedReader(new FileReader(file));

        try {
            int lines = countLines(pathname);
            questionAnswerMatr = new String[lines + 1][2];
            TimeUnit.SECONDS.sleep(s);

            int i = 0;
            System.out.println("CREAZIONE BASE DI CONOSCENZA");
            while ((st = br.readLine()) != null) {
                String[] splited = st.split("\\?");
                questionAnswerMatr[i] = splited;
                i++;
            }

        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Quiz.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                br.close();
                System.out.println("KB " + who + " creata.");
                System.out.println("Chiudo lo stream.");
                System.out.println();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    //Count the number of lines of a text file.
    public static int countLines(String filename) throws IOException {
        int count;
        boolean empty;
        try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
            byte[] c = new byte[1024];
            count = 0;
            int readChars = 0;
            empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
        }
        return (count == 0 && !empty) ? 1 : count;
    }
    
    //Getter for the quiz matrix
    public String[][] getQuestionAnswerMatr() {
        return questionAnswerMatr;
    }

    //Setter for a Question as already asked, so it won't be randomly chosen again.
    public void setQuestionUsed(int ind) {
        this.questionAnswerMatr[ind][0] = "USED";
    }

//ENDCLASS
}

