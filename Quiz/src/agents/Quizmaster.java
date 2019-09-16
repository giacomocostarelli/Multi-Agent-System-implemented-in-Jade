package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import quiz.Quiz;

/**
 * @author Giacomo
 */
public class Quizmaster extends Agent {

    private Quiz questionAnswer;
    private int pointsContestant1;
    private int pointsContestant2;
    private AID contestant1;
    private AID contestant2;

    @Override
    protected void setup() {
        prepareKBQ();
        registerServiceYPQ();
        searchForContestants();

        addBehaviour(new Behaviour() {
            private int step = 0;
            String question = "";

            public void action() {
                switch (step) {
                    case 0:
                        ACLMessage msg0 = new ACLMessage(ACLMessage.REQUEST);
                        msg0.addReceiver(new AID(contestant1.getLocalName(), AID.ISLOCALNAME));
                        msg0.addReceiver(new AID(contestant2.getLocalName(), AID.ISLOCALNAME));
                        question = generateQuestion();
                        System.out.println("[Quizmaster]: " + question + "?");
                        msg0.setContent(question);
                        send(msg0);
                        step++;
                        break;

                    case 1:
                        MessageTemplate mt0 = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
                        ACLMessage msg1 = receive(mt0);
                        if (msg1 != null) {
                            if (msg1.getSender().getLocalName().equals("Marco")) {
                                pointsContestant1++;
                            } else if (msg1.getSender().getLocalName().equals("Antonio")) {
                                pointsContestant2++;
                            }

                            System.out.println("[Quizmaster]: Molto bene! " + msg1.getSender().getLocalName() + " si aggiudica un punto!");
                            if (pointsContestant1 == 3 || pointsContestant2 == 3) {
                                System.out.println("\n\n[Quizmaster]: ATTENZIONE. Uno dei concorrenti ha guadgnato 3 punti ed ha quindi vinto."
                                        + "\n              Il quiz è concluso ed il vincitore è....");
                                if (pointsContestant1 == 3) {
                                    System.out.println("              MARCO!!!!");
                                } else {
                                    System.out.println("              ANTONIO!!!!");
                                }
                                System.out.println("[Quizmaster]: Congratulazioni sei l'agente più intelligente.");
                                System.out.println("[Quizmaster]: Dal vostro Quizmaster è tutto. Vi auguriamo una buona continuazione e ... alla prossima sfida!");
                            } else {
                                System.out.println("[Quizmaster]: Andiamo alla prossima domanda.\n");
                            }
                            step--;
                        } else {
                            block();
                        }
                        break;
                }
            }

            public boolean done() {
                return pointsContestant1 == 3 || pointsContestant2 == 3;
            }

            //END BEHAV   
        });

        addBehaviour(new Behaviour() {
            public void action() {
                if (pointsContestant1 == 3 || pointsContestant2 == 3) {
                    ACLMessage end = new ACLMessage(ACLMessage.CONFIRM);
                    end.addReceiver(new AID(contestant1.getLocalName(), AID.ISLOCALNAME));
                    end.addReceiver(new AID(contestant2.getLocalName(), AID.ISLOCALNAME));
                    send(end);
                    doDelete();
                }
            }

            public boolean done() {
                return pointsContestant1 == 3 || pointsContestant2 == 3;
            }
            //END BEHAV
        });

        //END SETUP
    }

    public String generateQuestion() {
        int lengthQuestion;
        String[][] kb = questionAnswer.getQuestionAnswerMatr();
        do {
            lengthQuestion = (int) (Math.random() * (kb.length - 1));
        } while (kb[lengthQuestion][0].equals("USED"));

        String question = kb[lengthQuestion][0];
        this.questionAnswer.setQuestionUsed(lengthQuestion);
        return question;
    }

    public void prepareKBQ() {
        try {
            pointsContestant1 = 0;
            pointsContestant2 = 0;
            questionAnswer = new Quiz(0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Contestant2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void registerServiceYPQ() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("asking-question");
        sd.setName("quizmaster");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException ex) {
            Logger.getLogger(Quizmaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void searchForContestants() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription templateSd = new ServiceDescription();
            templateSd.setType("answering-question");
            template.addServices(templateSd);
            TimeUnit.SECONDS.sleep(4);

            System.out.println("[Quizmaster]: sto cercando il nome dei partecipanti all'interno della YP.");
            DFAgentDescription[] results = DFService.search(this, template);

            if (results.length > 0) {
                contestant1 = results[0].getName();
                contestant2 = results[1].getName();
                System.out.println("[Quizmaster]: ho trovato due partecipanti al quiz: [" + contestant1.getLocalName() + "] e [" + contestant2.getLocalName() + "]");
                System.out.println("[Quizmaster]: Bene, io sono il Quizmaster e vi do' il benvenuto al Quiz più famoso tra gli agenti.");
                System.out.println("[Quizmaster]: *REGOLAMENTO*");
                System.out.println("[Quizmaster]: I partecipanti si sfideranno su domande di cultura generale e il primo a rispondere si aggiudicherà un punto!");
                System.out.println("[Quizmaster]: Il primo concorrente ad ottenere 3 punti avrà vinto la sfida.\n");
                System.out.println("[Quizmaster]: ... che il gioco abbia INIZIO!!!!\n");
            } else {
                System.out.println("[Quizmaster]: non ho trovato partecipanti iscritti al Quiz.");
            }
        } catch (FIPAException | InterruptedException ex) {
            Logger.getLogger(Quizmaster.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.exit(0);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
